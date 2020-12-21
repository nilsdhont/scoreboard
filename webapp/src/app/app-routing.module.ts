import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ScoreboardComponent } from "./scoreboard/scoreboard.component";


const routes: Routes = [
  { path: "scoreboard", component: ScoreboardComponent },
  { path: "", redirectTo: "/scoreboard", pathMatch: "full"}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
