import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../api.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-upload',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './upload.component.html',
    styleUrl: './upload.component.css'
})
export class UploadComponent implements OnInit {
    files: string[] = [];
    selectedFile: File | null = null;
    uploadStatus: string = '';

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadFiles();
    }

    loadFiles() {
        this.apiService.listDocuments().subscribe(files => this.files = files);
    }

    onFileSelected(event: any) {
        this.selectedFile = event.target.files[0];
    }

    onUpload() {
        if (!this.selectedFile) return;

        this.uploadStatus = 'Uploading...';
        this.apiService.uploadDocument(this.selectedFile).subscribe({
            next: (key) => {
                this.uploadStatus = 'Uploaded. Ingesting...';
                this.apiService.ingestDocument(key).subscribe({
                    next: () => {
                        this.uploadStatus = 'Done!';
                        this.loadFiles();
                    },
                    error: (err) => this.uploadStatus = 'Ingestion Failed: ' + err.message
                });
            },
            error: (err) => this.uploadStatus = 'Upload Failed: ' + err.message
        });
    }
}
