import { Component, Input, Output, EventEmitter, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { WebSocketService } from '../../core/services/websocket.service';

@Component({
  selector: 'app-log-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './log-viewer.component.html',
})
export class LogViewerComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() jobId!: string;
  @Output() onClose = new EventEmitter<void>();
  @ViewChild('logContainer') private logContainer!: ElementRef;

  logs: string[] = [];
  private subscription: Subscription | undefined;

  constructor(private wsService: WebSocketService) {}

  ngOnInit(): void {
    if (this.jobId) {
      this.subscription = this.wsService.watchTopic(`/topic/logs/job/${this.jobId}`).subscribe({
        next: (logLine) => {
          this.logs.push(logLine);
        },
        error: (err) => {
          this.logs.push(`[ERRO DE CONEXÃO WEBSOCKET] ${err}`);
        }
      });
    } else {
      this.logs.push("ID do Job não fornecido.");
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      if (this.logContainer) {
        this.logContainer.nativeElement.scrollTop = this.logContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  close() {
    this.onClose.emit();
  }
}