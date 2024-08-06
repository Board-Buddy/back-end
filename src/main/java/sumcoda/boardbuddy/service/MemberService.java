package sumcoda.boardbuddy.service;

import com.amazonaws.services.s3.AmazonS3Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sumcoda.boardbuddy.dto.*;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.entity.ProfileImage;
import sumcoda.boardbuddy.enumerate.Role;
import sumcoda.boardbuddy.exception.member.*;
import sumcoda.boardbuddy.exception.publicDistrict.PublicDistrictRetrievalException;
import sumcoda.boardbuddy.repository.member.MemberRepository;
import sumcoda.boardbuddy.repository.ProfileImageRepository;
import sumcoda.boardbuddy.repository.publicDistrict.PublicDistrictRepository;
import sumcoda.boardbuddy.util.FileStorageUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    private final PublicDistrictRepository publicDistrictRepository;

    private final ProfileImageRepository profileImageRepository;

    private final NearPublicDistrictService nearPublicDistrictService;

    private final PublicDistrictRedisService publicDistrictRedisService;

    // 비밀번호를 암호화 하기 위한 필드
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private final AmazonS3Client amazonS3Client;

    /**
     * 아이디 중복검사
     *
     * @param verifyUsernameDuplicationDTO 사용자가 입력한 아이디
     **/
    public void verifyUsernameDuplication(MemberRequest.VerifyUsernameDuplicationDTO verifyUsernameDuplicationDTO) {

        Boolean isAlreadyExistsUsername = memberRepository.existsByUsername(verifyUsernameDuplicationDTO.getUsername());

        if (isAlreadyExistsUsername == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsUsername)) {
            throw new UsernameAlreadyExistsException("동일한 아이디가 이미 존재합니다.");
        }
    }

    /**
     * 닉네임 중복검사
     *
     * @param verifyNicknameDuplicationDTO 사용자가 입력한 닉네임
     **/
    public void verifyNicknameDuplication(MemberRequest.VerifyNicknameDuplicationDTO verifyNicknameDuplicationDTO) {

        Boolean isAlreadyExistsNickname = memberRepository.existsByNickname(verifyNicknameDuplicationDTO.getNickname());

        if (isAlreadyExistsNickname == null) {
            throw new MemberRetrievalException("유저를 조회하면서 서버 문제가 발생했습니다. 관리자에게 문의하세요.");
        }

        if (Boolean.TRUE.equals(isAlreadyExistsNickname)) {
            throw new NicknameAlreadyExistsException("동일한 닉네임이 이미 존재합니다.");
        }
    }

    /**
     * 회원가입 요청 캐치
     *
     * @param registerDTO 전달받은 회원가입 정보
     **/
    @Transactional
    public void registerMember(MemberRequest.RegisterDTO registerDTO) {

        Long memberId = memberRepository.save(Member.buildMember(
                registerDTO.getUsername(),
                bCryptPasswordEncoder.encode(registerDTO.getPassword()),
                registerDTO.getNickname(),
                registerDTO.getEmail(),
                registerDTO.getPhoneNumber(),
                registerDTO.getSido(),
                registerDTO.getSgg(),
                registerDTO.getEmd(),
                2,
                50.0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                0.0,
                Role.USER,
                null)).getId();

        if (memberId == null) {
            throw new MemberSaveException("서버 문제로 회원가입에 실패하였습니다. 관리자에게 문의하세요.");
        }

        // 회원가입 시 주변 행정 구역 저장
        nearPublicDistrictService.saveNearDistrictByRegisterLocation(
                NearPublicDistrictRequest.LocationDTO.builder()
                        .sido(registerDTO.getSido())
                        .sgg(registerDTO.getSgg())
                        .emd(registerDTO.getEmd())
                        .build());
    }

    /**
     * 애플리케이션 시작시 관리자 계정 생성
     *
     **/
    public void createAdminAccount() {
        Boolean existsByUsername = memberRepository.existsByUsername("admin");
        if (existsByUsername) {
            return;
        }
        memberRepository.save(Member.buildMember(
                "admin",
                bCryptPasswordEncoder.encode("a12345#"),
                "admin",
                "admin@naver.com",
                "01012345678",
                "서울특별시",
                "강남구",
                "삼성동",
                2,
                50.0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                null,
                null,
                0.0,
                Role.USER,
                null)
        );
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param oAuth2RegisterDTO 소셜로그인 사용자에 대한 추가적인 회원가입 정보
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void registerOAuth2Member(MemberRequest.OAuth2RegisterDTO oAuth2RegisterDTO, String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));


        member.assignPhoneNumber(oAuth2RegisterDTO.getPhoneNumber());
        member.assignSido(oAuth2RegisterDTO.getSido());
        member.assignSgg(oAuth2RegisterDTO.getSgg());
        member.assignEmd(oAuth2RegisterDTO.getEmd());

        // 회원가입 시 주변 행정 구역 저장
        nearPublicDistrictService.saveNearDistrictByRegisterLocation(
                NearPublicDistrictRequest.LocationDTO.builder()
                        .sido(oAuth2RegisterDTO.getSido())
                        .sgg(oAuth2RegisterDTO.getSgg())
                        .emd(oAuth2RegisterDTO.getEmd())
                        .build());
    }

    /**
     * 소셜 로그인 사용자에 대한 추가적인 회원가입
     *
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void withdrawalMember(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        memberRepository.delete(member);

        // 삭제 확인
        boolean isExists = memberRepository.existsById(member.getId());
        if (isExists) {
            throw new MemberDeletionFailureException("회원 탈퇴에 실패했습니다. 관리자에게 문의하세요.");
        }
    }

    /**
     * 내 동네 조회 요청 캐치
     *
     * @param username 사용자 아이디
     * @return 사용자의 좌표, 반경, 주변 동네 정보가 포함된 DTO
     */
    public MemberResponse.MyLocationsDTO getMemberNeighborhoods(String username) {

        // 사용자 위치 및 반경 정보 조회
        MemberResponse.LocationWithRadiusDTO locationWithRadiusDTO = memberRepository.findLocationWithRadiusDTOByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 사용자의 위치 선언
        String sido = locationWithRadiusDTO.getSido();
        String sgg = locationWithRadiusDTO.getSgg();
        String emd = locationWithRadiusDTO.getEmd();

        // redis 에서 조회 - 기준 위치에 해당하는 CoordinateDTO 를 조회
        PublicDistrictResponse.CoordinateDTO coordinateDTO = publicDistrictRedisService.findCoordinateDTOBySidoAndSggAndEmd(sido, sgg, emd)
                .orElseGet(() -> {
                    // mariadb 에서 조회 - 기준 위치에 해당하는 CoordinateDTO 를 조회(redis 장애 발생 시 mariadb 에서 조회)
                    log.error("[redis findCoordinateDTOBySidoAndSggAndEmd() error]");
                    return publicDistrictRepository.findCoordinateDTOBySidoAndSggAndEmd(sido, sgg, emd)
                            .orElseThrow(() -> new PublicDistrictRetrievalException("입력한 위치 정보를 찾을 수 없습니다. 관리자에게 문의하세요."));
                });

        // 주변 동네 정보를 조회
        Map<Integer, List<MemberResponse.LocationDTO>> locations = nearPublicDistrictService.getNearbyLocations(
                NearPublicDistrictRequest.LocationDTO.builder()
                        .sido(sido)
                        .sgg(sgg)
                        .emd(emd)
                        .build());

        // 사용자의 좌표, 반경, 주변 동네 정보가 포함된 DTO 반환
        return MemberResponse.MyLocationsDTO.builder()
                .locations(locations)
                .longitude(coordinateDTO.getLongitude())
                .latitude(coordinateDTO.getLatitude())
                .radius(locationWithRadiusDTO.getRadius())
                .build();
    }

    /**
     * 내 동네 설정 요청 캐치
     *
     * @param locationDTO 사용자가 입력한 위치 정보
     * @param username 로그인 사용자 아이디
     * @return 주변 동네 정보가 포함된 DTO
     **/
    @Transactional
    public Map<Integer, List<MemberResponse.LocationDTO>> updateMemberNeighborhood(MemberRequest.LocationDTO locationDTO, String username) {

        // 사용자가 입력한 시도, 시구, 동
        String sido = locationDTO.getSido();
        String sgg = locationDTO.getSgg();
        String emd = locationDTO.getEmd();

        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 멤버의 위치 업데이트
        member.assignLocation(sido, sgg, emd);

        // 위치 설정 시 주변 행정 구역 저장 후 DTO 로 응답
        return nearPublicDistrictService.saveNearDistrictByUpdateLocation(
                NearPublicDistrictRequest.LocationDTO.builder()
                        .sido(sido)
                        .sgg(sgg)
                        .emd(emd)
                        .build());
    }

    /**
     * 내 반경 설정 요청 캐치
     *
     * @param radiusDTO 사용자가 입력한 반경 정보
     * @param username 로그인 사용자 아이디
     **/
    @Transactional
    public void updateMemberRadius(MemberRequest.RadiusDTO radiusDTO, String username) {
        // 사용자 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("해당 유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 멤버의 반경 업데이트
        member.assignRadius(radiusDTO.getRadius());
    }

    /**
     * 프로필 조회 요청 캐치
     *
     * @param nickname 유저 닉네임
     * @return 해당 닉네임의 유저 프로필
     **/
    public MemberResponse.ProfileInfosDTO getMemberProfileByNickname(String nickname) {

        if (nickname == null) {
            throw new MemberNotFoundException("해당 유저를 찾을 수 없습니다.");
        }

        return memberRepository.findMemberProfileByNickname(nickname)
                .orElseThrow(() -> new MemberRetrievalException("프로필을 조회할 수 없습니다. 관리자에게 문의하세요."));
    }

    /**
     * 프로필 수정 요청 캐치
     *
     * @param username 유저 아이디
     * @param updateProfileDTO 수정할 정보가 담겨있는 DTO
     * @param profileImageFile 수정할 프로필 이미지 파일
     **/
    @Transactional
    public void updateProfile(String username, MemberRequest.UpdateProfileDTO updateProfileDTO, MultipartFile profileImageFile) {
        // 유저 아이디로 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberRetrievalException("유저를 찾을 수 없습니다. 관리자에게 문의하세요."));

        // 닉네임이 null이 아니면 업데이트
        if (updateProfileDTO.getNickname() != null) {
            member.assignNickname(updateProfileDTO.getNickname());
        }

        // 비밀번호가 null이 아니면 암호화 후 업데이트
        if (updateProfileDTO.getPassword() != null && !updateProfileDTO.getPassword().isEmpty()) {
            member.assignPassword(bCryptPasswordEncoder.encode(updateProfileDTO.getPassword()));
        }

        // 핸드폰 번호가 null이 아니면 업데이트
        if (updateProfileDTO.getPhoneNumber() != null) {
            member.assignPhoneNumber(updateProfileDTO.getPhoneNumber());
        }

        // 자기소개가 null이 아니면 업데이트
        if (updateProfileDTO.getDescription() != null) {
            member.assignDescription(updateProfileDTO.getDescription());
        }

        if (profileImageFile == null || profileImageFile.isEmpty()) {
            member.assignProfileImage(null);
        } else {
            // 이미지 파일 형식 검증
            String contentType = profileImageFile.getContentType();
            if (contentType != null && !contentType.startsWith("multipart/form-data")) {
                throw new InvalidFileFormatException("지원되지 않는 파일 형식입니다.");
            }
            try {
                FileDTO fileDTO = FileStorageUtil.saveFile(profileImageFile);
                File file = fileDTO.getFile();

                // 프로필 이미지 로컬 저장 시 필요
                String profileImageUrl = FileStorageUtil.getLocalStoreDir(fileDTO.getSavedFilename());

                ProfileImage newProfileImage = ProfileImage.buildProfileImage(
                        fileDTO.getOriginalFilename(),
                        fileDTO.getSavedFilename(),
                        profileImageUrl
                );

                profileImageRepository.save(newProfileImage);
                member.assignProfileImage(newProfileImage);

                file.delete();

//                 // 프로필 이미지 S3 저장 시 필요
//                String bucketName = "";
//                ProfileImage existingProfileImage = member.getProfileImage();
//
//                // 기존 프로필 이미지가 있다면 S3에서 삭제
//                if (existingProfileImage != null) {
//                    amazonS3Client.deleteObject(bucketName, existingProfileImage.getSavedFilename());
//                }
//
//                amazonS3Client.putObject(bucketName, fileDTO.getSavedFilename(), file);
//
//                String awsS3URL = amazonS3Client.getUrl(bucketName, fileDTO.getSavedFilename()).toString();
//
//                ProfileImage newProfileImage = ProfileImage.buildProfileImage(
//                        fileDTO.getOriginalFilename(),
//                        fileDTO.getSavedFilename(),
//                        awsS3URL
//                );
//
//                profileImageRepository.save(newProfileImage);
//                member.assignProfileImage(newProfileImage);
//
//                file.delete();

            } catch (IOException e) {
                throw new ProfileImageSaveException("프로필 이미지를 저장하는 동안 오류가 발생했습니다.");
            }
        }
    }
}
