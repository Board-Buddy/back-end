package sumcoda.boardbuddy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumcoda.boardbuddy.dto.CommentRequest;
import sumcoda.boardbuddy.dto.CommentResponse;
import sumcoda.boardbuddy.dto.GatherArticleResponse;
import sumcoda.boardbuddy.dto.MemberResponse;
import sumcoda.boardbuddy.entity.Comment;
import sumcoda.boardbuddy.entity.GatherArticle;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.exception.comment.*;
import sumcoda.boardbuddy.exception.gatherArticle.GatherArticleNotFoundException;
import sumcoda.boardbuddy.exception.member.MemberRetrievalException;
import sumcoda.boardbuddy.repository.MemberRepository;
import sumcoda.boardbuddy.repository.comment.CommentRepository;
import sumcoda.boardbuddy.repository.gatherArticle.GatherArticleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;

    private final GatherArticleRepository gatherArticleRepository;

    private final MemberRepository memberRepository;

    /**
     * 댓글 작성
     *
     * @param gatherArticleId 모집글 ID
     * @param parentId        부모 댓글 ID (없을 경우 null)
     * @param createDTO       댓글 작성 요청 DTO
     * @param username        사용자 이름
     */
    @Transactional
    public void createComment(Long gatherArticleId, Long parentId, CommentRequest.CreateDTO createDTO, String username) {

        // 사용자 검증
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 모집글 검증
        GatherArticle gatherArticle = gatherArticleRepository.findById(gatherArticleId)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 댓글 내용
        String content = createDTO.getContent();

        // 댓글 내용 검증
        if (content == null || content.isEmpty()) {
            throw new CommentSaveException("댓글이 입력되지 않았습니다.");
        }

        // 댓글 및 대댓글 검증
        Comment parentComment = null;
        if (parentId != null) {
            parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글입니다."));

            // 대댓글의 대댓글 작성 방지
            if (parentComment.getParent() != null) {
                throw new CommentLevelException("대댓글의 대댓글은 작성할 수 없습니다.");
            }
        }

        // 댓글 생성
        Comment comment = Comment.buildComment(
                content,
                member,
                gatherArticle,
                parentComment);

        // 댓글 저장
        commentRepository.save(comment);
    }

    /**
     * 댓글 조회
     *
     * @param gatherArticleId 모집글 ID
     * @param username        사용자 이름
     * @return 댓글 리스트
     */
    public List<CommentResponse.CommentDTO> getComments(Long gatherArticleId, String username) {

        // 멤버 검증
        if (!memberRepository.existsByUsername(username)) {
            throw new MemberRetrievalException("서버 문제로 사용자의 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
        }

        // 모집글 검증
        GatherArticleResponse.IdDTO idDTO = gatherArticleRepository.findIdDTOById(gatherArticleId)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 댓글 조회
        return commentRepository.findCommentDTOsByGatherArticleId(idDTO.getId());
    }

    /**
     * 댓글 수정
     *
     * @param gatherArticleId 모집글 ID
     * @param commentId       댓글 ID
     * @param updateDTO       댓글 수정 요청 DTO
     * @param username        사용자 이름
     */
    @Transactional
    public void updateComment(Long gatherArticleId, Long commentId, CommentRequest.UpdateDTO updateDTO, String username) {

        // 사용자 검증
        MemberResponse.UserNameDTO userNameDTO = memberRepository.findUserNameDTOByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 모집글 검증
        GatherArticleResponse.IdDTO idDTO = gatherArticleRepository.findIdDTOById(gatherArticleId)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 댓글 검증
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글입니다."));

        // 모집글 안에 댓글이 있는지 검증
        if (!comment.getGatherArticle().getId().equals(idDTO.getId())) {
            throw new CommentNotInGatherArticleException("해당 모집글에 속하지 않는 댓글입니다.");
        }

        // 작성자인지 검증
        if (!comment.getMember().getUsername().equals(userNameDTO.getUsername())) {
            throw new CommentAccessException("자신의 댓글만 수정할 수 있습니다.");
        }

        // 댓글 내용
        String content = updateDTO.getContent();

        // 댓글 내용 검증
        if (content == null || content.isEmpty()) {
            throw new CommentUpdateException("댓글이 입력되지 않았습니다.");
        }

        // 댓글 업데이트
        comment.assignContent(content);
    }

    /**
     * 댓글 삭제
     *
     * @param gatherArticleId 모집글 ID
     * @param commentId       댓글 ID
     * @param username        사용자 이름
     */
    @Transactional
    public void deleteComment(Long gatherArticleId, Long commentId, String username) {

        // 사용자 검증
        MemberResponse.UserNameDTO userNameDTO = memberRepository.findUserNameDTOByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유효하지 않은 사용자입니다."));

        // 모집글 검증
        GatherArticleResponse.IdDTO idDTO = gatherArticleRepository.findIdDTOById(gatherArticleId)
                .orElseThrow(() -> new GatherArticleNotFoundException("존재하지 않는 모집글입니다."));

        // 댓글 검증
        Comment comment = commentRepository.findCommentByCommentId(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글입니다."));

        // 모집글 안에 댓글이 있는지 검증
        if (!comment.getGatherArticle().getId().equals(idDTO.getId())) {
            throw new CommentNotInGatherArticleException("해당 모집글에 속하지 않는 댓글입니다.");
        }

        // 작성자인지 검증
        if (!comment.getMember().getUsername().equals(userNameDTO.getUsername())) {
            throw new CommentAccessException("자신의 댓글만 삭제할 수 있습니다.");
        }

        // 자식 댓글 선언
        List<Comment> children = comment.getChildren();

        // 자식 댓글이 있으면 자식 댓글 먼저 삭제
        if (!children.isEmpty()) {
            commentRepository.deleteAllInBatch(children);
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }
}
