import { Component } from '@angular/core';
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
export class ChatComponent {
    messages: Message[] = [];
    userInput: string = '';
    loading: boolean = false;

    constructor(private apiService: ApiService) { }

    sendMessage() {
        if (!this.userInput.trim()) return;

        const query = this.userInput;
        this.messages.push({ text: query, sender: 'user' });
        this.userInput = '';
        this.loading = true;

        this.apiService.chat(query).subscribe({
            next: (response) => {
                this.messages.push({ text: response, sender: 'ai' });
                this.loading = false;
            },
            error: (err) => {
                this.messages.push({ text: 'Error: ' + err.message, sender: 'ai' });
                this.loading = false;
            }
        });
    }
}
