package com.todayyu.persistence.inquiry.adapter;

import com.todayyu.application.inquiry.dto.InquiryCursor;
import com.todayyu.application.inquiry.port.dto.InquirySummaryView;
import com.todayyu.application.inquiry.port.read.ReadInquiryPort;
import com.todayyu.application.inquiry.port.save.SaveInquiryPort;
import com.todayyu.domain.inquiry.entity.Inquiry;
import com.todayyu.domain.inquiry.vo.InquiryId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.inquiry.dto.InquirySummaryViewProjection;
import com.todayyu.persistence.inquiry.entity.InquiryJpaEntity;
import com.todayyu.persistence.inquiry.mapper.InquiryMapper;
import com.todayyu.persistence.inquiry.repository.InquiryJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaInquiryAdapter implements ReadInquiryPort, SaveInquiryPort {
    private final InquiryJpaRepository inquiryRepository;
    private final InquiryMapper inquiryMapper;
    
    /**
     * 문의 ID({@link InquiryId})를 통해 문의({@link Inquiry})를 조회하는 메서드
     *
     * @param id 문의 ID
     * @return 문의
     */
    @Override
    public Optional<Inquiry> readById(InquiryId id) {
        return inquiryRepository.findById(id.getValue())
                .map(inquiryMapper::toDomain);
    }
    
    /**
     * 회원이 작성한 문의 중 커서({@link InquiryCursor}) 이후의 문의 목록을 최신 작성 순으로 조회해
     * 문의 요약({@link InquirySummaryView}) 목록으로 반환하는 메서드
     *
     * <p> 커서가 {@code null}이면 처음부터 조회한다. </p>
     *
     * @param memberId 문의 작성 회원 ID
     * @param cursor   마지막으로 조회한 문의의 커서
     * @param limit    최대 조회 개수
     * @return 문의 요약 목록
     */
    @Override
    public List<InquirySummaryView> readByCursor(MemberId memberId, InquiryCursor cursor, int limit) {
        Long cursorId = (cursor == null) ? null : cursor.id().getValue();
        
        List<InquirySummaryViewProjection> projections
                = inquiryRepository.findByCursor(memberId.getValue(), cursorId, limit);
        
        return projections.stream()
                .map(InquirySummaryViewProjection::toView)
                .toList();
    }
    
    /**
     * 문의를 저장하는 메서드
     *
     * @param inquiry 문의 객체
     * @return 저장 후 {@link InquiryId}가 부여된 문의 객체
     */
    @Override
    public Inquiry save(Inquiry inquiry) {
        InquiryJpaEntity savedJpaEntity = inquiryRepository.save(inquiryMapper.toJpaEntity(inquiry));
        
        return inquiryMapper.toDomain(savedJpaEntity);
    }

    /**
     * 문의의 연락용 이메일, 제목, 내용, 수정 시각을 수정하는 메서드
     *
     * <p> 영속 상태의 {@link InquiryJpaEntity}를 조회해 필드를 변경하며, 트랜잭션 커밋 시 변경 감지를 통해 반영된다. </p>
     *
     * @param inquiry 수정된 문의 객체
     * @return 수정된 문의 객체
     * @throws IllegalStateException 문의 ID에 해당하는 문의가 존재하지 않는 경우
     */
    @Override
    public Inquiry update(Inquiry inquiry) {
        InquiryJpaEntity jpaEntity = inquiryRepository.findById(inquiry.getId().getValue())
                .orElseThrow(() -> new IllegalStateException("문의가 존재하지 않습니다. inquiryId = " + inquiry.getId().getValue()));
        
        jpaEntity.update(
                inquiry.getContactEmail().getValue(),
                inquiry.getTitle().getValue(),
                inquiry.getContent().getValue(),
                inquiry.getUpdatedAt()
        );
        
        return inquiryMapper.toDomain(jpaEntity);
    }
    
    /**
     * 문의 ID({@link InquiryId})에 해당하는 문의를 삭제하는 메서드
     *
     * <p> 해당 문의가 존재하지 않으면 아무것도 하지 않는다. </p>
     *
     * @param id 삭제할 문의 ID
     */
    @Override
    public void deleteById(InquiryId id) {
        inquiryRepository.deleteById(id.getValue());
    }
}
