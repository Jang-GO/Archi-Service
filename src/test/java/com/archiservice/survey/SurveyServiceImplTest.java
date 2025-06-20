package com.archiservice.survey;

import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.jwt.JwtUtil;
import com.archiservice.common.jwt.RefreshTokenService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.BusinessException;
import com.archiservice.exception.ErrorCode;
import com.archiservice.survey.domain.Option;
import com.archiservice.survey.domain.Question;
import com.archiservice.survey.dto.QuestionHistoryDto;
import com.archiservice.survey.dto.response.QuestionResponseDto;
import com.archiservice.survey.repository.QuestionRepository;
import com.archiservice.survey.service.impl.SurveyServiceImpl;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SurveyServiceImplTest {

    private static final Logger log = LoggerFactory.getLogger(SurveyServiceImplTest.class);
    @Mock
    private QuestionRepository questionRepository;
    @Mock private UserRepository userRepository;
    @Mock private TagMetaService metaService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private SurveyServiceImpl surveyService;
    
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    @DisplayName("첫 번째 질문 요청 - 세션 초기화")
    void getQuestion_ShouldInitializeSessionWhenFirstQuestion() {
        // given
        Long questionId = 1L;
        Question mockQuestion = createMockQuestion(1L, "첫 번째 질문", List.of("답변1", "답변2"));
        
        when(questionRepository.findById(1L)).thenReturn(Optional.of(mockQuestion));

        // when
        ApiResponse<QuestionResponseDto> result = surveyService.getQuestion(questionId, 10L, false, session);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(result.getData().getQuestionText()).isEqualTo("첫 번째 질문");
        
        // 세션 상태 확인
        assertThat(session.getAttribute("tagCodeSum")).isEqualTo(10L);
        List<QuestionHistoryDto> history = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getQuestionId()).isEqualTo(1L);
        assertThat(history.get(0).getTagCode()).isEqualTo(10L);
    }

    @Test
    @DisplayName("일반 질문 요청 - 태그 코드 누적")
    void getQuestion_ShouldAccumulateTagCode() {
        // given
        session.setAttribute("tagCodeSum", 10L);
        session.setAttribute("questionHistory", new ArrayList<>());
        
        Question mockQuestion = createMockQuestion(2L, "두 번째 질문", List.of("답변1", "답변2"));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(mockQuestion));

        // when
        ApiResponse<QuestionResponseDto> result = surveyService.getQuestion(2L, 20L, false, session);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(session.getAttribute("tagCodeSum")).isEqualTo(30L); // 10 + 20
        
        List<QuestionHistoryDto> history = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getQuestionId()).isEqualTo(2L);
        assertThat(history.get(0).getTagCode()).isEqualTo(20L);
    }

    @Test
    @DisplayName("설문 종료 - nextQuestionId가 null")
    void getQuestion_ShouldEndSurveyWhenNextQuestionIdIsNull() {
        // given
        session.setAttribute("tagCodeSum", 15L);
        when(metaService.extractTagsFromCode(15L)).thenReturn(List.of("음식", "여행"));

        // when
        ApiResponse<QuestionResponseDto> result = surveyService.getQuestion(null, null, false, session);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(result.getData().getQuestionText()).isEqualTo("성향 테스트 종료");
        assertThat(result.getData().getOrder()).isEqualTo(0);
        assertThat(result.getData().getOptions()).isEmpty();
        assertThat(result.getData().getTagCodes()).containsExactly("음식", "여행");
    }

    @Test
    @DisplayName("존재하지 않는 질문 요청 시 예외 발생")
    void getQuestion_ShouldThrowExceptionWhenQuestionNotFound() {
        // given
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> surveyService.getQuestion(999L, 10L, false, session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);
    }

    @Test
    @DisplayName("이전 답변에서 온 요청 - 히스토리에 추가 안 함")
    void getQuestion_ShouldNotAddToHistoryWhenFromPrevious() {
        // given
        List<QuestionHistoryDto> initialHistory = new ArrayList<>();
        initialHistory.add(new QuestionHistoryDto(1L, 10L));
        session.setAttribute("questionHistory", initialHistory);
        session.setAttribute("tagCodeSum", 10L);
        
        Question mockQuestion = createMockQuestion(2L, "두 번째 질문", List.of("답변1", "답변2"));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(mockQuestion));

        // when
        ApiResponse<QuestionResponseDto> result = surveyService.getQuestion(2L, 20L, true, session);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        
        List<QuestionHistoryDto> history = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
        assertThat(history).hasSize(1); // 히스토리에 추가되지 않음
        assertThat(session.getAttribute("tagCodeSum")).isEqualTo(30L); // 태그 코드는 여전히 누적
    }

    @Test
    @DisplayName("설문 결과 저장 성공")
    void saveResult_ShouldSaveUserTagCodeAndReturnToken() {
        // given
        Long userId = 1L;
        session.setAttribute("tagCodeSum", 25L);
        
        User mockUser = createMockUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(any(CustomUser.class))).thenReturn("new-access-token");

        // when
        ApiResponse<String> result = surveyService.saveResult(userId, session);

        // then
        assertThat(result.getResultCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("new-access-token");
        
        verify(mockUser).setTagCode(25L);
        verify(userRepository).save(mockUser);
        
        // 세션 정리 확인
        assertThat(session.getAttribute("tagCodeSum")).isNull();
        assertThat(session.getAttribute("questionHistory")).isNull();
    }

    @Test
    @DisplayName("저장된 태그코드 없이 결과 저장 시 예외 발생")
    void saveResult_ShouldThrowExceptionWhenNoTagCodeInSession() {
        // given
        Long userId = 1L;
        User mockUser = createMockUser(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> surveyService.saveResult(userId, session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND)
                .hasMessage("저장된 태그코드가 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 결과 저장 시 예외 발생")
    void saveResult_ShouldThrowExceptionWhenUserNotFound() {
        // given
        session.setAttribute("tagCodeSum", 25L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> surveyService.saveResult(999L, session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    // TODO : 처리안됨
//    @Test
//    @DisplayName("이전 질문으로 이동 성공")
//    void getPreviousQuestion_ShouldReturnPreviousQuestion() {
//        // given
//        List<QuestionHistoryDto> history = new ArrayList<>();
//        history.add(new QuestionHistoryDto(1L, 10L));
//        history.add(new QuestionHistoryDto(2L, 20L));
//        log.debug(history.toString());
//        session.setAttribute("questionHistory", history);
//        session.setAttribute("tagCodeSum", 30L);
//
//        Question mockQuestion = createMockQuestion(1L, "첫 번째 질문", List.of("답변1", "답변2"));
//        when(questionRepository.findById(1L)).thenReturn(Optional.of(mockQuestion));
//
//        // when
//
//        ApiResponse<QuestionResponseDto> result = surveyService.getPreviousQuestion(session);
//
//        // then
//        assertThat(result.getResultCode()).isEqualTo(200);
//        assertThat(result.getData().getQuestionText()).isEqualTo("첫 번째 질문");
//
//        // 히스토리에서 마지막 항목 제거됨
//        List<QuestionHistoryDto> updatedHistory = (List<QuestionHistoryDto>) session.getAttribute("questionHistory");
//        log.debug(updatedHistory.toString());
//        assertThat(updatedHistory).hasSize(1);
//        assertThat(updatedHistory.get(0).getQuestionId()).isEqualTo(1L);
//
//        // 태그 코드에서 이전 값 차감됨
//        assertThat(session.getAttribute("tagCodeSum")).isEqualTo(10L); // 30 - 20
//    }

    @Test
    @DisplayName("이전 질문이 없을 때 예외 발생")
    void getPreviousQuestion_ShouldThrowExceptionWhenNoPreviousQuestion() {
        // given
        List<QuestionHistoryDto> history = new ArrayList<>();
        history.add(new QuestionHistoryDto(1L, 10L));
        session.setAttribute("questionHistory", history);

        // when & then
        assertThatThrownBy(() -> surveyService.getPreviousQuestion(session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessage("이전 질문이 없습니다.");
    }

    @Test
    @DisplayName("히스토리가 없을 때 이전 질문 요청 시 예외 발생")
    void getPreviousQuestion_ShouldThrowExceptionWhenNoHistory() {
        // when & then
        assertThatThrownBy(() -> surveyService.getPreviousQuestion(session))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT_VALUE)
                .hasMessage("이전 질문이 없습니다.");
    }

    private Question createMockQuestion(Long id, String text, List<String> answers) {
        Question question = mock(Question.class);
        when(question.getSurveyQuestionId()).thenReturn(id);
        when(question.getQuestionText()).thenReturn(text);
        when(question.getQuestionOrder()).thenReturn(1);

        // Option 객체들을 Mock으로 생성
        List<Option> options = answers.stream()
                .map(answer -> {
                    Option option = mock(Option.class);
                    when(option.getOptionText()).thenReturn(answer);
                    return option;
                })
                .toList();

        when(question.getOptions()).thenReturn(options);
        return question;
    }

    private User createMockUser(Long id) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        return user;
    }
}