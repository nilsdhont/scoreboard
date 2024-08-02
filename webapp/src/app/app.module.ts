import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import { provideHttpClient, withInterceptorsFromDi } from "@angular/common/http";
import {AppComponent} from './app.component';
import {ScoreboardComponent} from './scoreboard/scoreboard.component';

@NgModule({ declarations: [
        AppComponent,
        ScoreboardComponent
    ],
    bootstrap: [AppComponent], imports: [BrowserModule,
        AppRoutingModule], providers: [provideHttpClient(withInterceptorsFromDi())] })
export class AppModule {
}
