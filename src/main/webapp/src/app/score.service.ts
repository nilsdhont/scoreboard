import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";


export class MatchData {
  nameBrigandZe: string
  scoreBrigandZe: number
  nameVisitors: string
  scoreVisitors : number

}

@Injectable({
  providedIn: 'root'
})
export class ScoreService {

  constructor(private http: HttpClient) {
  }

  getCurrentMatchData(): Observable<MatchData> {
    return this.http.get<MatchData>(`/matchData`);
  }
}
