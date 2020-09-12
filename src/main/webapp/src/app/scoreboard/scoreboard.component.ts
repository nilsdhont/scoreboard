import {Component, OnInit} from '@angular/core';
import {ScoreService} from "../score.service";
import {interval} from 'rxjs'
import {HttpErrorResponse} from "@angular/common/http";

export class MatchData {
  nameBrigandZe: string = "BrigandZe";
  scoreBrigandZe: number = 0;
  nameVisitors: string = "Visitors";
  scoreVisitors: number = 0;
}

@Component({
  selector: 'app-scoreboard',
  templateUrl: './scoreboard.component.html',
  styleUrls: ['./scoreboard.component.scss']
})
export class ScoreboardComponent implements OnInit {

  matchData: MatchData

  fontSize: number;

  constructor(private scoreService: ScoreService) {
  }

  ngOnInit(): void {
    this.matchData = new MatchData();
    this.updateScoreBoard();
  }

  private updateScoreBoard() {
    if (this.matchData.scoreBrigandZe >= 100 || this.matchData.scoreVisitors >= 100) {
      this.fontSize = 73;
    } else {
      this.fontSize = 80;
    }

    interval(1000 * 5).subscribe(() => {
      this.scoreService.getCurrentMatchData().subscribe(response => {
          this.matchData.nameBrigandZe = response.nameBrigandZe
          this.matchData.scoreBrigandZe = response.scoreBrigandZe
          this.matchData.nameVisitors = response.nameVisitors;
          this.matchData.scoreVisitors = response.scoreVisitors;

          if (this.matchData.scoreBrigandZe >= 100 || this.matchData.scoreVisitors >= 100) {
            this.fontSize = 60;
          } else {
            this.fontSize = 50;
          }
        },
        (error: HttpErrorResponse) => console.log(error));
    });


  }
}
