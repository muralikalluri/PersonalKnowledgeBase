import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { UploadComponent } from './components/upload/upload.component';
import { ChatComponent } from './components/chat/chat.component';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [CommonModule, UploadComponent, ChatComponent],
    template: `
    <div class="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8">
      <div class="max-w-4xl mx-auto">
        <header class="mb-8 text-center">
          <h1 class="text-3xl font-bold text-gray-900">Personal Knowledge Base</h1>
          <p class="text-gray-600 mt-2">Upload PDFs and clear your doubts using AI</p>
        </header>
        
        <div class="grid grid-cols-1 gap-8">
          <app-upload></app-upload>
          <app-chat></app-chat>
        </div>
      </div>
    </div>
  `
})
export class AppComponent {
    title = 'frontend';
}
