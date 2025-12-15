import { Component, ElementRef, ViewChild, ChangeDetectorRef, AfterViewChecked } from '@angular/core';
import { ApiService } from '../../api.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Message {
    text: string;
    sender: 'user' | 'ai';
}

@Component({
    selector: 'app-chat',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chat.component.html',
    styleUrl: './chat.component.css'
})
export class ChatComponent implements AfterViewChecked {
    @ViewChild('chatContainer') private chatContainer!: ElementRef;
    messages: Message[] = [];
    userInput: string = '';
    loading: boolean = false;

    constructor(
        private apiService: ApiService,
        private cd: ChangeDetectorRef
    ) { }

    ngAfterViewChecked() {
        this.scrollToBottom();
    }

    scrollToBottom(): void {
        try {
            if (this.chatContainer) {
                this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
            }
        } catch (err) { }
    }

    sendMessage() {
        if (!this.userInput.trim()) return;

        const query = this.userInput;
        this.messages.push({ text: query, sender: 'user' });
        this.userInput = '';
        this.loading = true;
        this.scrollToBottom();

        this.apiService.chat(query).subscribe({
            next: (response) => {
                console.log('Chat Response:', response);
                this.messages.push({ text: response.answer, sender: 'ai' });
                this.loading = false;
                this.cd.detectChanges(); // Force update
                this.scrollToBottom();
            },
            error: (err) => {
                console.error('Chat Error:', err);
                this.messages.push({ text: 'Error: ' + err.message, sender: 'ai' });
                this.loading = false;
                this.cd.detectChanges(); // Force update
                this.scrollToBottom();
            }
        });
    }
}
