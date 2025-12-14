import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class ApiService {

    constructor(private http: HttpClient) { }

    // Document Service
    uploadDocument(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post('/api/docs', formData, { responseType: 'text' });
    }

    listDocuments(): Observable<string[]> {
        return this.http.get<string[]>('/api/docs');
    }

    // Ingestion Service
    ingestDocument(key: String): Observable<any> {
        return this.http.post(`/api/ingest?key=${key}`, {}, { responseType: 'text' });
    }

    // Chat Service
    chat(query: string): Observable<string> {
        return this.http.get('/api/chat', {
            params: { query },
            responseType: 'text'
        });
    }
}
