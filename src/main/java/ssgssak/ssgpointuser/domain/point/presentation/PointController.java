package ssgssak.ssgpointuser.domain.point.presentation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ssgssak.ssgpointuser.domain.point.application.PointServiceImpl;
import ssgssak.ssgpointuser.domain.point.dto.*;
import ssgssak.ssgpointuser.domain.point.entity.PointType;
import ssgssak.ssgpointuser.domain.point.vo.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/point")
@Slf4j
public class PointController {

    private final PointServiceImpl pointService;
    private final ModelMapper modelMapper;


    /**
     * 포인트 컨트롤러
     * 1. 포인트 적립 - 스토어
     * 2. 포인트 적립 - 파트너
     * 3. 포인트 선물받기
     * 4. 포인트 전환하기
     * 5. 포인트 기간별로 조회하기
     * 6. 사용가능 포인트 조회
     * 7. 포인트 적립 - 이벤트
     * 8. 이벤트 당일 중복확인 (오늘 날짜로 조회해서 있다면 중복이다)
     */


    // 1. 포인트 적립 - 스토어 -> 진행이후 스토어포인트에 POST, "/storepoint/add"로 요청이 들어가는것까지가 한세트임
    @PostMapping("/add/store")
    public ResponseEntity<PointIdOutVo> addPointStore(@RequestBody PointAddInVo addInVo, Principal principal) {
        PointIdOutDto outDto =
                pointService.pointAddStore(modelMapper.map(addInVo, CreatePointDto.class), principal.getName());
        PointIdOutVo outVo = modelMapper.map(outDto, PointIdOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 2. 포인트 적립 - 파트너 -> 진행이후 파트너포인트에 POST, "/partnerpoint/add"로 요청이 들어가는것까지가 한세트임
    @PostMapping("/add/partner")
    public ResponseEntity<PointIdOutVo> addPointPartner(@RequestBody PointAddInVo addInVo, Principal principal) {
        PointIdOutDto outDto =
                pointService.pointAddPartner(modelMapper.map(addInVo, CreatePointDto.class), principal.getName());
        PointIdOutVo outVo = modelMapper.map(outDto, PointIdOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 3. 포인트 선물받기 -> 진행이후 선물포인트에 POST, "/gift/accept"로 요청이 들어가는것가지가 한세트임
    @PostMapping("/gift/receive")
    public ResponseEntity<PointGiftAcceptOutVo> receivePoint(@RequestBody PointGiftAcceptInVo inVo, Principal principal) {
        PointGiftAcceptResponseDto responseDto = pointService.receiveGiftPoint(
                modelMapper.map(inVo, PointGiftAcceptRequestDto.class), principal.getName());
        PointGiftAcceptOutVo outVo = modelMapper.map(responseDto, PointGiftAcceptOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 4. 포인트 전환하기 -> 진행이후 전환포인트에 POST, "/exchangepoint/add"로 요청이 들어가는것까지가 한세트임
    @PostMapping("/exchange")
    public ResponseEntity<PointIdOutVo> exchangePoint(@RequestBody PointAddInVo addInVo, Principal principal) {
        PointIdOutDto outDto =
                pointService.pointExchange(modelMapper.map(addInVo, CreatePointDto.class), principal.getName());
        PointIdOutVo outVo = modelMapper.map(outDto, PointIdOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 5. 포인트 기간별로 조회하기
    @GetMapping("/list")
    public ResponseEntity<PointListOutVo> searchPointList(PointListInVo inVo, Principal principal) {
        PointListRequestDto requestDto = modelMapper.map(inVo, PointListRequestDto.class);
        PointListResponseDto responseDto = pointService.pointSearch(requestDto, principal.getName());
        PointListOutVo outVo = modelMapper.map(responseDto, PointListOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 6. 사용 가능 포인트 조회
    @GetMapping("/possible")
    public ResponseEntity<PointPossibleOutVo> searchPossiblePoint(Principal principal) {
        PointPossibleResponseDto responseDto = pointService.searchPossible(principal.getName());
        PointPossibleOutVo outVo = modelMapper.map(responseDto, PointPossibleOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 7. 포인트 적립 - 이벤트
    @PostMapping("/add/event")
    public ResponseEntity<PointEventOutVo> addPointEvent(@RequestBody PointAddInVo addInVo,
                                                         @RequestParam(required = false) Integer continueDay,
                                                         Principal principal) {
        PointEventOutDto outDto =
                pointService.pointAddEvent(modelMapper.map(addInVo, CreatePointDto.class), principal.getName(), continueDay);
        PointEventOutVo outVo = modelMapper.map(outDto, PointEventOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }

    // 8. 이벤트 당일 중복확인 (오늘 날짜로 조회해서 있다면 중복이다)
    @GetMapping("/duplicate/event")
    public ResponseEntity<CheckDuplicateOutVo> checkDuplicateEvent(@RequestParam PointType type, Principal principal) {
        CheckDuplicateDto duplicateDto = pointService.checkDuplicate(principal.getName(), type);
        CheckDuplicateOutVo outVo = modelMapper.map(duplicateDto, CheckDuplicateOutVo.class);
        return new ResponseEntity<>(outVo, HttpStatus.OK);
    }
}
