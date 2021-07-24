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
    this.setFontSize()

    interval(1000 * 5).subscribe(() => {
      this.scoreService.getCurrentMatchData().subscribe(response => {
          this.matchData.nameBrigandZe = response.nameBrigandZe
          this.matchData.scoreBrigandZe = response.scoreBrigandZe
          this.matchData.nameVisitors = response.nameVisitors;
          this.matchData.scoreVisitors = response.scoreVisitors;

          this.setFontSize()
        },
        (error: HttpErrorResponse) => console.log(error));
    });


  }

  private setFontSize() {
    if (this.matchData.scoreBrigandZe >= 100 || this.matchData.scoreVisitors >= 100) {
      this.fontSize = 46;
    } else {
      this.fontSize = 58;
    }

    if (this.matchData.nameVisitors.length > 11) {
      this.matchData.nameVisitors = this.matchData.nameVisitors.substr(0, 11)
    }
  }
}
