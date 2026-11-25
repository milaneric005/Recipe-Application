import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { switchMap } from 'rxjs';
import { LookupGroup, LookupGroupOption, LookupType } from '../../models/lookup-type';
import { LookupTypeService } from '../../services/lookup-type.service';

type SortField = 'id' | 'name';
type SortDirection = 'asc' | 'desc';

@Component({
  selector: 'app-admin-types',
  templateUrl: './admin-types.html',
  styleUrl: './admin-types.css',
})
export class AdminTypes {
  private readonly lookupTypeService = inject(LookupTypeService);

  readonly groups: LookupGroupOption[] = [
    { key: 'dish-types', label: 'Dish types' },
    { key: 'meal-types', label: 'Meal types' },
    { key: 'diet-types', label: 'Diet types' },
    { key: 'ingredient-categories', label: 'Ingredient categories' },
    { key: 'allergens', label: 'Allergens' },
  ];
  readonly pageSize = 5;

  readonly selectedGroup = signal<LookupGroup>('dish-types');
  readonly search = signal('');
  readonly sortField = signal<SortField>('id');
  readonly sortDirection = signal<SortDirection>('asc');
  readonly page = signal(1);

  private readonly items = toSignal(
    toObservable(this.selectedGroup).pipe(
      switchMap((group) => this.lookupTypeService.getAll(group)),
    ),
    { initialValue: [] as LookupType[] },
  );

  readonly filteredItems = computed(() => {
    const term = this.search().trim().toLowerCase();
    const field = this.sortField();
    const direction = this.sortDirection() === 'asc' ? 1 : -1;
    return this.items()
      .filter((item) => item.name.toLowerCase().includes(term))
      .sort((a, b) =>
        field === 'id' ? (a.id - b.id) * direction : a.name.localeCompare(b.name) * direction,
      );
  });

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredItems().length / this.pageSize)),
  );

  readonly pagedItems = computed(() => {
    const start = (this.page() - 1) * this.pageSize;
    return this.filteredItems().slice(start, start + this.pageSize);
  });

  readonly selectedLabel = computed(
    () => this.groups.find((group) => group.key === this.selectedGroup())?.label ?? '',
  );

  selectGroup(group: LookupGroup): void {
    this.selectedGroup.set(group);
    this.search.set('');
    this.page.set(1);
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.page.set(1);
  }

  sortBy(field: SortField): void {
    if (this.sortField() === field) {
      this.sortDirection.update((direction) => (direction === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortField.set(field);
      this.sortDirection.set('asc');
    }
    this.page.set(1);
  }

  sortIndicator(field: SortField): string {
    if (this.sortField() !== field) {
      return '';
    }
    return this.sortDirection() === 'asc' ? '▲' : '▼';
  }

  previousPage(): void {
    this.page.update((page) => Math.max(1, page - 1));
  }

  nextPage(): void {
    this.page.update((page) => Math.min(this.totalPages(), page + 1));
  }
}
