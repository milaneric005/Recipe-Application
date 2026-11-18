-- =====================================================================
-- RecipeWebApp - PostgreSQL šema
-- Pokretanje:
--   psql -U postgres -c "CREATE DATABASE recipe_app;"
--   psql -U postgres -d recipe_app -f recipe_app.sql
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

BEGIN;

-- ---------------------------------------------------------------------
-- 1. Šifarnici (upravlja administrator)
-- ---------------------------------------------------------------------
CREATE TABLE meal_types (
    id   int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE dish_types (
    id   int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE diet_types (
    id   int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE ingredient_categories (
    id   int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE allergens (
    id   int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE units (
    id           int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name         varchar(50) NOT NULL UNIQUE,
    abbreviation varchar(15) NOT NULL UNIQUE
);

CREATE TABLE preparation_types (
    id                int GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              varchar(100) NOT NULL UNIQUE,
    requires_duration boolean NOT NULL DEFAULT true   -- false: bez termičke obrade, ostalo
);

-- ---------------------------------------------------------------------
-- 2. Korisnici i autentifikacija
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username          varchar(50)  NOT NULL,
    email             varchar(255) NOT NULL,
    password_hash     varchar(255) NOT NULL,
    role              text NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    is_active         boolean NOT NULL DEFAULT false,        -- true nakon aktivacionog mejla
    profile_image_url text,
    bio               text,
    region_name       varchar(150),
    region_lat        double precision CHECK (region_lat BETWEEN -90 AND 90),
    region_lng        double precision CHECK (region_lng BETWEEN -180 AND 180),
    created_at        timestamptz NOT NULL DEFAULT now(),
    CHECK ((region_lat IS NULL) = (region_lng IS NULL))
);
CREATE UNIQUE INDEX ux_users_username ON users (lower(username));
CREATE UNIQUE INDEX ux_users_email    ON users (lower(email));

CREATE TABLE user_tokens (
    id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    purpose    text NOT NULL CHECK (purpose IN ('ACTIVATION', 'PASSWORD_RESET')),
    token_hash varchar(128) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    used_at    timestamptz
);
CREATE INDEX ix_user_tokens_user ON user_tokens (user_id);

-- ---------------------------------------------------------------------
-- 3. Namirnice i alergeni
-- ---------------------------------------------------------------------
CREATE TABLE ingredients (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        varchar(150) NOT NULL,
    category_id int NOT NULL REFERENCES ingredient_categories(id),
    unit_id     int NOT NULL REFERENCES units(id),            -- podrazumevana merna jedinica
    type        text NOT NULL DEFAULT 'GENERIC' CHECK (type IN ('GENERIC', 'BRANDED')),
    text_color  char(7) CHECK (text_color ~ '^#[0-9A-Fa-f]{6}$'),  -- boja teksta za brendirane
    quantity_optional boolean NOT NULL DEFAULT false,         -- "so po ukusu"
    barcode     varchar(50),
    CHECK (type = 'BRANDED' OR text_color IS NULL)
);
CREATE UNIQUE INDEX ux_ingredients_name ON ingredients (lower(name));
CREATE INDEX ix_ingredients_category    ON ingredients (category_id);
CREATE INDEX ix_ingredients_name_trgm   ON ingredients USING gin (name gin_trgm_ops);

CREATE TABLE ingredient_allergens (
    ingredient_id bigint NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
    allergen_id   int    NOT NULL REFERENCES allergens(id)   ON DELETE CASCADE,
    PRIMARY KEY (ingredient_id, allergen_id)
);

CREATE TABLE category_suggestions (
    id                      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id                 bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_name               varchar(150) NOT NULL,            -- element za koji treba nova kategorija
    suggested_name          varchar(100) NOT NULL,
    explanation             text,
    status                  text NOT NULL DEFAULT 'PENDING'
                            CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'ALTERNATIVE')),
    assigned_category_id    int REFERENCES ingredient_categories(id) ON DELETE SET NULL,
    admin_note              text,
    handled_by              bigint REFERENCES users(id) ON DELETE SET NULL,
    created_at              timestamptz NOT NULL DEFAULT now(),
    handled_at              timestamptz
);
CREATE INDEX ix_category_suggestions_status ON category_suggestions (status);

-- ---------------------------------------------------------------------
-- 4. Recepti
-- ---------------------------------------------------------------------
CREATE TABLE recipes (
    id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_id           bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               varchar(200) NOT NULL,
    dish_type_id        int NOT NULL REFERENCES dish_types(id),
    servings            smallint NOT NULL CHECK (servings > 0),
    total_time_minutes  int NOT NULL DEFAULT 0 CHECK (total_time_minutes >= 0),  -- zbir koraka (trigger)
    video_url           text,
    video_is_upload     boolean NOT NULL DEFAULT false,       -- true: otpremljen fajl, false: link
    story               text,                                 -- "Priča o receptu"
    -- poreklo recepta (koordinate se čuvaju da se geokodiranje ne ponavlja)
    origin_country      varchar(100),
    origin_region       varchar(100),
    origin_city         varchar(100),
    origin_lat          double precision CHECK (origin_lat BETWEEN -90 AND 90),
    origin_lng          double precision CHECK (origin_lng BETWEEN -180 AND 180),
    -- region autora u trenutku objave (promena regiona ne menja stare recepte)
    author_region_name  varchar(150),
    author_region_lat   double precision,
    author_region_lng   double precision,
    -- modifikacija postojećeg recepta
    original_recipe_id  bigint REFERENCES recipes(id) ON DELETE SET NULL,
    modification_note   text,
    is_removed          boolean NOT NULL DEFAULT false,       -- uklonjen sadržaj (moderacija)
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now(),
    CHECK ((origin_lat IS NULL) = (origin_lng IS NULL)),
    CHECK (original_recipe_id IS NULL OR original_recipe_id <> id)
);
CREATE INDEX ix_recipes_author     ON recipes (author_id);
CREATE INDEX ix_recipes_original   ON recipes (original_recipe_id);
CREATE INDEX ix_recipes_dish       ON recipes (dish_type_id);
CREATE INDEX ix_recipes_created    ON recipes (created_at DESC);
CREATE INDEX ix_recipes_time       ON recipes (total_time_minutes);
CREATE INDEX ix_recipes_title_trgm ON recipes USING gin (title gin_trgm_ops);

CREATE TABLE recipe_meal_types (
    recipe_id    bigint NOT NULL REFERENCES recipes(id)    ON DELETE CASCADE,
    meal_type_id int    NOT NULL REFERENCES meal_types(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, meal_type_id)
);
CREATE INDEX ix_recipe_meal_types_type ON recipe_meal_types (meal_type_id);

CREATE TABLE recipe_diet_types (
    recipe_id    bigint NOT NULL REFERENCES recipes(id)    ON DELETE CASCADE,
    diet_type_id int    NOT NULL REFERENCES diet_types(id) ON DELETE CASCADE,
    PRIMARY KEY (recipe_id, diet_type_id)
);
CREATE INDEX ix_recipe_diet_types_type ON recipe_diet_types (diet_type_id);

CREATE TABLE recipe_ingredients (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipe_id     bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    ingredient_id bigint NOT NULL REFERENCES ingredients(id),
    quantity      numeric(12,3) CHECK (quantity > 0),         -- NULL: "so po ukusu"
    unit_id       int REFERENCES units(id),
    position      smallint NOT NULL DEFAULT 0,
    CHECK (quantity IS NULL OR unit_id IS NOT NULL)
);
CREATE INDEX ix_recipe_ingredients_recipe     ON recipe_ingredients (recipe_id);
CREATE INDEX ix_recipe_ingredients_ingredient ON recipe_ingredients (ingredient_id);

CREATE TABLE recipe_steps (
    id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipe_id           bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    step_number         smallint NOT NULL CHECK (step_number > 0),
    title               varchar(200),
    description         text NOT NULL,
    preparation_type_id int NOT NULL REFERENCES preparation_types(id),
    duration_minutes    int CHECK (duration_minutes > 0),
    CONSTRAINT ux_recipe_steps_order UNIQUE (recipe_id, step_number) DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX ix_recipe_steps_prep ON recipe_steps (preparation_type_id);

-- ---------------------------------------------------------------------
-- 5. Omiljeni, ocene, komentari, fotografije
-- ---------------------------------------------------------------------
CREATE TABLE favorite_recipes (
    user_id    bigint NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    recipe_id  bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, recipe_id)
);

CREATE TABLE ratings (
    recipe_id  bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    user_id    bigint NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    score      smallint NOT NULL CHECK (score BETWEEN 1 AND 5),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (recipe_id, user_id)
);
CREATE INDEX ix_ratings_updated ON ratings (updated_at);

CREATE TABLE comments (
    id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipe_id  bigint NOT NULL REFERENCES recipes(id)  ON DELETE CASCADE,
    author_id  bigint NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    parent_id  bigint REFERENCES comments(id) ON DELETE CASCADE,   -- ugnježdeni odgovori
    body       text NOT NULL,
    is_edited  boolean NOT NULL DEFAULT false,
    is_removed boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_comments_recipe ON comments (recipe_id, created_at);
CREATE INDEX ix_comments_parent ON comments (parent_id);

-- jedna tabela za sve fotografije: recept (glavna/dodatne), korak ili komentar
CREATE TABLE photos (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uploaded_by bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    url         text NOT NULL,
    recipe_id   bigint REFERENCES recipes(id)      ON DELETE CASCADE,
    step_id     bigint REFERENCES recipe_steps(id) ON DELETE CASCADE,
    comment_id  bigint REFERENCES comments(id)     ON DELETE CASCADE,
    is_main     boolean NOT NULL DEFAULT false,
    sort_order  smallint NOT NULL DEFAULT 0,
    is_removed  boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(recipe_id, step_id, comment_id) = 1),
    CHECK (NOT is_main OR recipe_id IS NOT NULL)
);
CREATE UNIQUE INDEX ux_photos_main_per_recipe ON photos (recipe_id) WHERE is_main;
CREATE INDEX ix_photos_recipe  ON photos (recipe_id);
CREATE INDEX ix_photos_step    ON photos (step_id);
CREATE INDEX ix_photos_comment ON photos (comment_id);

-- ---------------------------------------------------------------------
-- 6. Prijave neprikladnog sadržaja
-- ---------------------------------------------------------------------
CREATE TABLE reports (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reporter_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type       text NOT NULL CHECK (target_type IN ('RECIPE', 'COMMENT', 'PHOTO')),
    target_recipe_id  bigint REFERENCES recipes(id)  ON DELETE SET NULL,
    target_comment_id bigint REFERENCES comments(id) ON DELETE SET NULL,
    target_photo_id   bigint REFERENCES photos(id)   ON DELETE SET NULL,
    reason            text NOT NULL,
    status            text NOT NULL DEFAULT 'NEW'
                      CHECK (status IN ('NEW', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    rejection_reason  text,
    handled_by        bigint REFERENCES users(id) ON DELETE SET NULL,
    created_at        timestamptz NOT NULL DEFAULT now(),
    handled_at        timestamptz,
    CONSTRAINT ck_report_target CHECK (
        (target_type = 'RECIPE'  AND target_comment_id IS NULL AND target_photo_id IS NULL) OR
        (target_type = 'COMMENT' AND target_recipe_id  IS NULL AND target_photo_id IS NULL) OR
        (target_type = 'PHOTO'   AND target_recipe_id  IS NULL AND target_comment_id IS NULL)
    ),
    CONSTRAINT ck_report_rejection CHECK (status <> 'REJECTED' OR rejection_reason IS NOT NULL)
);
CREATE INDEX ix_reports_status   ON reports (status);
CREATE INDEX ix_reports_reporter ON reports (reporter_id);

-- ---------------------------------------------------------------------
-- 7. Evidencija namirnica korisnika
-- ---------------------------------------------------------------------
CREATE TABLE user_ingredients (
    id                 bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ingredient_id      bigint NOT NULL REFERENCES ingredients(id),
    brand              varchar(100),
    quantity           numeric(12,3) CHECK (quantity > 0),
    unit_id            int REFERENCES units(id),
    expiry_date        date,
    expiry_notified_at timestamptz,                          -- da se obaveštenje ne šalje dvaput
    barcode            varchar(50),
    is_synced          boolean NOT NULL DEFAULT false,
    CHECK (quantity IS NULL OR unit_id IS NOT NULL)
);
CREATE INDEX ix_user_ingredients_user   ON user_ingredients (user_id);
CREATE INDEX ix_user_ingredients_expiry ON user_ingredients (expiry_date) WHERE expiry_date IS NOT NULL;

-- ---------------------------------------------------------------------
-- 8. Liste za kupovinu
-- ---------------------------------------------------------------------
CREATE TABLE shopping_lists (
    id               bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name             varchar(150) NOT NULL,
    created_at       timestamptz NOT NULL DEFAULT now(),
    planned_at       timestamptz,                            -- planirani datum/vreme (podsetnik)
    is_permanent     boolean NOT NULL DEFAULT false,         -- trajno aktivna lista
    status           text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    archived_at      timestamptz,
    reminder_sent_at timestamptz
);
CREATE INDEX ix_shopping_lists_user ON shopping_lists (user_id, status);
CREATE INDEX ix_shopping_lists_planned ON shopping_lists (planned_at)
    WHERE planned_at IS NOT NULL AND status = 'ACTIVE';

CREATE TABLE shopping_list_items (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    list_id       bigint NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    ingredient_id bigint NOT NULL REFERENCES ingredients(id),
    quantity      numeric(12,3) CHECK (quantity > 0),
    unit_id       int REFERENCES units(id),
    is_purchased  boolean NOT NULL DEFAULT false,
    CONSTRAINT ux_list_ingredient UNIQUE (list_id, ingredient_id),
    CHECK (quantity IS NULL OR unit_id IS NOT NULL)
);

-- ---------------------------------------------------------------------
-- 9. Notifikacije
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        text NOT NULL CHECK (type IN (
                    'REPORT_NEW', 'REPORT_STATUS',
                    'CATEGORY_SUGGESTION_NEW', 'CATEGORY_SUGGESTION_STATUS',
                    'INGREDIENT_EXPIRY', 'SHOPPING_REMINDER', 'TIMER')),
    message     text NOT NULL,
    target_type text CHECK (target_type IN
                    ('RECIPE', 'COMMENT', 'REPORT', 'CATEGORY_SUGGESTION', 'SHOPPING_LIST', 'USER_INGREDIENT')),
    target_id   bigint,                                      -- polimorfna referenca (bez FK)
    is_read     boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_notifications_user ON notifications (user_id, created_at DESC);
CREATE INDEX ix_notifications_unread ON notifications (user_id) WHERE NOT is_read;

-- aktivnosti korisnika (pregled / kuvanje recepta)
CREATE TABLE user_activities (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     bigint NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    recipe_id   bigint NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
    type        text NOT NULL CHECK (type IN ('VIEW', 'COOKING')),
    occurred_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ix_user_activities_user   ON user_activities (user_id, occurred_at DESC);
CREATE INDEX ix_user_activities_recipe ON user_activities (recipe_id);

-- ---------------------------------------------------------------------
-- 10. Trigeri
-- ---------------------------------------------------------------------
CREATE FUNCTION fn_set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recipes_updated  BEFORE UPDATE ON recipes  FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_comments_updated BEFORE UPDATE ON comments FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_ratings_updated  BEFORE UPDATE ON ratings  FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- region autora se kopira u recept pri kreiranju
CREATE FUNCTION fn_recipe_author_region() RETURNS trigger AS $$
BEGIN
    IF NEW.author_region_name IS NULL THEN
        SELECT region_name, region_lat, region_lng
          INTO NEW.author_region_name, NEW.author_region_lat, NEW.author_region_lng
          FROM users WHERE id = NEW.author_id;
    END IF;
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recipe_author_region BEFORE INSERT ON recipes
    FOR EACH ROW EXECUTE FUNCTION fn_recipe_author_region();

-- trajanje je obavezno osim za tipove pripreme sa requires_duration = false
CREATE FUNCTION fn_step_validate() RETURNS trigger AS $$
DECLARE
    v_required boolean;
BEGIN
    SELECT requires_duration INTO v_required FROM preparation_types WHERE id = NEW.preparation_type_id;
    IF v_required AND NEW.duration_minutes IS NULL THEN
        RAISE EXCEPTION 'Trajanje je obavezno za izabrani tip pripreme' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_step_validate BEFORE INSERT OR UPDATE ON recipe_steps
    FOR EACH ROW EXECUTE FUNCTION fn_step_validate();

-- ukupno vreme recepta = zbir trajanja koraka
CREATE FUNCTION fn_recipe_total_time() RETURNS trigger AS $$
DECLARE
    v_recipe bigint;
BEGIN
    IF TG_OP = 'DELETE' THEN v_recipe := OLD.recipe_id; ELSE v_recipe := NEW.recipe_id; END IF;
    UPDATE recipes
       SET total_time_minutes = COALESCE((SELECT sum(duration_minutes) FROM recipe_steps
                                           WHERE recipe_id = v_recipe), 0)
     WHERE id = v_recipe;
    RETURN NULL;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recipe_total_time AFTER INSERT OR UPDATE OR DELETE ON recipe_steps
    FOR EACH ROW EXECUTE FUNCTION fn_recipe_total_time();

-- lista (koja nije trajna) se automatski arhivira kada je sve kupljeno
CREATE FUNCTION fn_shopping_auto_archive() RETURNS trigger AS $$
BEGIN
    UPDATE shopping_lists
       SET status = 'ARCHIVED', archived_at = now()
     WHERE id = NEW.list_id
       AND status = 'ACTIVE'
       AND NOT is_permanent
       AND NOT EXISTS (SELECT 1 FROM shopping_list_items
                        WHERE list_id = NEW.list_id AND NOT is_purchased);
    RETURN NULL;
END $$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shopping_auto_archive AFTER UPDATE OF is_purchased ON shopping_list_items
    FOR EACH ROW EXECUTE FUNCTION fn_shopping_auto_archive();

-- ---------------------------------------------------------------------
-- 11. Pogledi i funkcije
-- ---------------------------------------------------------------------
-- alergeni recepta (automatski, na osnovu namirnica)
CREATE VIEW recipe_allergens AS
SELECT DISTINCT ri.recipe_id, ia.allergen_id
  FROM recipe_ingredients ri
  JOIN ingredient_allergens ia ON ia.ingredient_id = ri.ingredient_id;

-- prosečna ocena i broj ocena
CREATE VIEW recipe_rating_stats AS
SELECT recipe_id, round(avg(score), 2) AS avg_score, count(*) AS rating_count
  FROM ratings
 GROUP BY recipe_id;

-- popularni recepti: najveća prosečna ocena u poslednjih mesec dana
CREATE VIEW popular_recipes_30d AS
SELECT recipe_id, round(avg(score), 2) AS avg_score, count(*) AS rating_count
  FROM ratings
 WHERE updated_at >= now() - interval '30 days'
 GROUP BY recipe_id;

-- pretraga po dostupnim namirnicama: recepti kojima nedostaje najviše p_max_missing namirnica
CREATE FUNCTION recipes_by_available_ingredients(p_ingredient_ids bigint[], p_max_missing int DEFAULT 0)
RETURNS TABLE (recipe_id bigint, total_ingredients int, missing_count int) AS $$
    SELECT r.id,
           count(DISTINCT ri.ingredient_id)::int,
           (count(DISTINCT ri.ingredient_id)
              FILTER (WHERE ri.ingredient_id <> ALL (p_ingredient_ids)))::int
      FROM recipes r
      JOIN recipe_ingredients ri ON ri.recipe_id = r.id
     WHERE NOT r.is_removed
     GROUP BY r.id
    HAVING count(DISTINCT ri.ingredient_id)
              FILTER (WHERE ri.ingredient_id <> ALL (p_ingredient_ids)) <= p_max_missing
$$ LANGUAGE sql STABLE;

-- ---------------------------------------------------------------------
-- 12. Početni podaci
-- ---------------------------------------------------------------------
INSERT INTO meal_types (name) VALUES
    ('Doručak'), ('Ručak'), ('Večera'), ('Glavna jela'),
    ('Predjela'), ('Salate'), ('Deserti'), ('Pića');

INSERT INTO dish_types (name) VALUES
    ('Supa'), ('Čorba'), ('Testenina'), ('Pica'), ('Sendvič'), ('Burger'), ('Rižoto'),
    ('Jelo od mesa'), ('Jelo od ribe'), ('Jelo od povrća'), ('Pekarski proizvod'),
    ('Kolač'), ('Torta'), ('Palačinke');

INSERT INTO diet_types (name) VALUES
    ('Vegetarijanska'), ('Veganska'), ('Posna'), ('Mrsna'), ('Hrono'), ('Peskatarijanska');

INSERT INTO ingredient_categories (name) VALUES
    ('Povrće'), ('Voće'), ('Meso'), ('Riba'),
    ('Mlečni proizvodi'), ('Žitarice'), ('Začini'), ('Orašasti plodovi');

INSERT INTO allergens (name) VALUES
    ('Mleko'), ('Gluten'), ('Kikiriki'), ('Jaja'), ('Soja'), ('Orašasti plodovi'), ('Riba');

INSERT INTO units (name, abbreviation) VALUES
    ('gram', 'g'), ('kilogram', 'kg'), ('mililitar', 'ml'), ('litar', 'l'),
    ('komad', 'kom'), ('kašika', 'kaš'), ('kašičica', 'kašič'), ('prstohvat', 'prst');

INSERT INTO preparation_types (name, requires_duration) VALUES
    ('Kuvanje', true), ('Pečenje', true), ('Prženje', true), ('Dinstanje', true),
    ('Grilovanje', true), ('Pohovanje', true), ('Kuvanje na pari', true),
    ('Priprema bez termičke obrade', false), ('Ostalo', false);

-- Unapred određen administrator (password_hash generiši u backendu, npr. bcrypt):
-- INSERT INTO users (username, email, password_hash, role, is_active)
-- VALUES ('admin', 'admin@example.com', '<BCRYPT_HASH>', 'ADMIN', true);

COMMIT;