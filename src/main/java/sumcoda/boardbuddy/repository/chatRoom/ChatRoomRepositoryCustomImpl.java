package sumcoda.boardbuddy.repository.chatRoom;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import sumcoda.boardbuddy.dto.ChatRoomResponse;
import sumcoda.boardbuddy.dto.fetch.ChatMessageLastSentInfoProjection;
import sumcoda.boardbuddy.dto.fetch.ChatRoomInfoProjection;
import sumcoda.boardbuddy.dto.fetch.GatherArticleSimpleInfoProjection;
import sumcoda.boardbuddy.entity.QChatMessage;
import sumcoda.boardbuddy.entity.QChatRoom;

import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QChatMessage.chatMessage;
import static sumcoda.boardbuddy.entity.QChatRoom.*;
import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberChatRoom.memberChatRoom;

@RequiredArgsConstructor
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;


    /**
     * 특정 모집글 Id로 ChatRoom 검증 정보 조회
     *
     * @param gatherArticleId 모집글 Id
     * @return ChatRoom 검증 정보가 포함된 ValidateDTO 객체
     **/
    @Override
    public Optional<ChatRoomResponse.ValidateDTO> findValidateDTOByGatherArticleId(Long gatherArticleId) {
        return Optional.ofNullable(jpaQueryFactory.select(Projections.fields(ChatRoomResponse.ValidateDTO.class,
                        chatRoom.id))
                .from(chatRoom)
                .join(chatRoom.gatherArticle, gatherArticle)
                .where(gatherArticle.id.eq(gatherArticleId))
                .fetchOne());
    }

    /**
     * 특정 사용자 아이디 사용자가 속한 채팅방 상세 정보 목록 조회
     *
     * @param username 사용자 아이디
     * @return 사용자가 속한 채팅방의 상세 정보 목록
     **/
    @Override
    public List<ChatRoomInfoProjection> findChatRoomInfoProjectionsByUsername(String username) {

        QChatMessage subQueryChatMessage = new QChatMessage("subQueryChatMessage");

        QChatRoom subQueryChatRoom = new QChatRoom("subQueryChatRoom");

        return jpaQueryFactory
                .select(Projections.constructor(ChatRoomInfoProjection.class,
                        chatRoom.id.as("chatRoomId"),
                        Projections.constructor(GatherArticleSimpleInfoProjection.class,
                                gatherArticle.id.as("gatherArticleId"),
                                gatherArticle.title,
                                gatherArticle.meetingLocation,
                                gatherArticle.currentParticipants
                        ),
                        Projections.constructor(ChatMessageLastSentInfoProjection.class,
                                chatMessage.content,
                                chatMessage.createdAt.as("sentAt")
                        )
                ))
                .from(memberChatRoom)
                .join(memberChatRoom.member, member)
                .join(memberChatRoom.chatRoom, chatRoom)
                .leftJoin(chatRoom.chatMessages, chatMessage)
                .join(chatRoom.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(chatMessage.createdAt.in(
                                JPAExpressions
                                        .select(subQueryChatMessage.createdAt.max())
                                        .from(subQueryChatMessage)
                                        .leftJoin(subQueryChatMessage.chatRoom, subQueryChatRoom)
                                        .where(subQueryChatRoom.id.eq(chatRoom.id))
                        ))
                )
                .orderBy(chatMessage.createdAt.desc())
                .fetch();
    }
}
