package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.mappers.InvestmentMapper;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InvestmentRepositoryPort;
import br.com.controlei.domain.models.dtos.investment.CreateInvestmentRequest;
import br.com.controlei.domain.models.dtos.investment.InvestmentQueryFilter;
import br.com.controlei.domain.models.dtos.investment.InvestmentResponse;
import br.com.controlei.domain.models.dtos.investment.UpdateInvestmentRequest;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.Investment;
import br.com.controlei.domain.models.enums.CategoryType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InvestmentService {

    private final InvestmentRepositoryPort investmentRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final InvestmentMapper investmentMapper;
    private final AuthorizationService authorizationService;

    public InvestmentService(InvestmentRepositoryPort investmentRepository,
                             CategoryRepositoryPort categoryRepository,
                             InvestmentMapper investmentMapper,
                             AuthorizationService authorizationService) {
        this.investmentRepository = investmentRepository;
        this.categoryRepository = categoryRepository;
        this.investmentMapper = investmentMapper;
        this.authorizationService = authorizationService;
    }

    public List<InvestmentResponse> listInvestments(InvestmentQueryFilter filter) {
        UUID familyId = authorizationService.currentFamilyId();
        return investmentRepository.findAllByFamilyIdAndFilters(familyId, filter.userId(), filter.type(),
                        filter.categoryId(), filter.startDate(), filter.endDate())
                .stream()
                .map(investmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        UUID familyId = authorizationService.currentFamilyId();

        authorizationService.requireCanWrite(familyId, request.userId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(familyId)) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.INVESTMENT) {
                throw new BusinessException("Categoria deve ser do tipo INVESTIMENTO");
            }
        }

        Investment investment = investmentMapper.toEntity(request, familyId);
        Investment saved = investmentRepository.save(investment);
        return investmentMapper.toResponse(saved);
    }

    public InvestmentResponse getInvestment(UUID id) {
        Investment investment = findActiveById(id);
        authorizationService.requireSameFamily(investment.getFamilyId());
        return investmentMapper.toResponse(investment);
    }

    @Transactional
    public InvestmentResponse updateInvestment(UUID id, UpdateInvestmentRequest request) {
        Investment investment = findActiveById(id);

        authorizationService.requireCanWrite(investment.getFamilyId(), investment.getUserId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!category.getFamilyId().equals(investment.getFamilyId())) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.INVESTMENT) {
                throw new BusinessException("Categoria deve ser do tipo INVESTIMENTO");
            }
        }

        investmentMapper.updateEntity(investment, request);
        Investment saved = investmentRepository.save(investment);
        return investmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteInvestment(UUID id) {
        Investment investment = findActiveById(id);
        authorizationService.requireCanWrite(investment.getFamilyId(), investment.getUserId());
        investment.setDeletedAt(LocalDateTime.now());
        investment.setDeletedBy(authorizationService.currentUserId().toString());
        investmentRepository.save(investment);
    }

    private Investment findActiveById(UUID id) {
        return investmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Investimento nao encontrado"));
    }
}
