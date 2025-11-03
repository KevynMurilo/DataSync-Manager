import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService extends RxStomp {

  constructor() {
    super();
    this.configure({
      brokerURL: 'ws://localhost:8082/ws-backup',
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
    this.activate();
  }

  public watchTopic(topic: string): Observable<string> {
    return this.watch(topic).pipe(
      map(message => message.body)
    );
  }
}