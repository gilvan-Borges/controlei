export type BankConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'ERROR' | 'SYNCING';

export interface BankConnection {
  id: string;
  familyId: string;
  userId: string;
  userName?: string;
  institutionId: string;
  institutionName: string;
  externalItemId: string;
  status: BankConnectionStatus;
  lastSyncedAt?: string;
  createdAt: string;
}

export interface ConnectBankRequest {
  institutionId: string;
  institutionName: string;
  externalItemId: string;
  targetAccountId?: string;
  targetCreditCardId?: string;
}

export interface SyncTransactionsResponse {
  connectionId: string;
  newTransactionsImported: number;
  duplicatesSkipped: number;
  message: string;
}
