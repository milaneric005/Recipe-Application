import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { LOOKUP_TYPES_MOCK } from '../mock/lookup-types.mock';
import { LookupGroup, LookupType } from '../models/lookup-type';

@Injectable({ providedIn: 'root' })
export class LookupTypeService {
  getAll(group: LookupGroup): Observable<LookupType[]> {
    return of(LOOKUP_TYPES_MOCK[group]);
  }
}
