import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { UserService } from '../../../../core/services/user.service';
import { AccountType } from '../../../../core/models/account.model';
import { User } from '../../../../core/models/user.model';

@Component({
  selector: 'app-account-form',
  standalone: false,
  templateUrl: './account-form.component.html',
  styleUrl: './account-form.component.scss'
})
export class AccountFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  saving = false;
  errorMessage = '';
  isEdit = false;
  accountId: string | null = null;
  users: User[] = [];

  accountTypes: { value: AccountType; label: string }[] = [
    { value: 'CHECKING', label: 'Corrente' },
    { value: 'SAVINGS', label: 'Poupança' },
    { value: 'CASH', label: 'Dinheiro' },
    { value: 'INVESTMENT', label: 'Investimento' },
    { value: 'OTHER', label: 'Outro' }
  ];

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.accountId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.accountId;

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(255)]],
      type: ['CHECKING', [Validators.required]],
      shared: [false, [Validators.required]],
      userId: [null],
      initialBalance: [0, [Validators.required, Validators.min(0)]],
      active: [true]
    });

    this.loadUsers();

    if (this.isEdit) {
      this.loadAccount();
    }

    this.form.get('shared')!.valueChanges.subscribe(shared => {
      if (shared) {
        this.form.get('userId')!.setValue(null);
      }
    });
  }

  private loadUsers(): void {
    this.userService.listUsers().subscribe({
      next: (users) => this.users = users,
      error: () => {}
    });
  }

  private loadAccount(): void {
    this.loading = true;
    this.accountService.getAccount(this.accountId!).subscribe({
      next: (account) => {
        this.form.patchValue({
          name: account.name,
          type: account.type,
          shared: account.shared,
          userId: account.userId,
          initialBalance: account.initialBalance,
          active: account.active
        });
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.message || 'Erro ao carregar conta.';
        this.loading = false;
      }
    });
  }

  get showUserField(): boolean {
    return !this.form.get('shared')?.value;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    const formValue = this.form.value;
    const request: any = {
      name: formValue.name,
      type: formValue.type,
      shared: formValue.shared,
      initialBalance: formValue.initialBalance
    };
    if (!formValue.shared && formValue.userId) {
      request.userId = formValue.userId;
    }

    if (this.isEdit) {
      request.active = formValue.active;
      this.accountService.updateAccount(this.accountId!, request).subscribe({
        next: () => this.router.navigate(['/app/accounts']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao atualizar conta.';
        }
      });
    } else {
      this.accountService.createAccount(request).subscribe({
        next: () => this.router.navigate(['/app/accounts']),
        error: (err) => {
          this.saving = false;
          this.errorMessage = err?.message || 'Erro ao criar conta.';
        }
      });
    }
  }
}
