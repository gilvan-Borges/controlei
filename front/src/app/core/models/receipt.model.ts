export type ScanStatus = 'PROCESSING' | 'COMPLETED' | 'NEEDS_REVIEW' | 'FAILED';

export interface ReceiptScan {
  id: string;
  attachmentId: string;
  fileName?: string;
  rawText?: string;
  extractedAmount?: number;
  extractedDate?: string;
  extractedMerchant?: string;
  suggestedCategoryId?: string;
  suggestedCategoryName?: string;
  status: ScanStatus;
  confidenceScore?: number;
  createdAt: string;
}

export interface SimulateScanRequest {
  receiptText: string;
}
