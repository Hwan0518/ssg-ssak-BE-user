package ssgssak.ssgpointuser.domain.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssgssak.ssgpointuser.domain.point.dto.*;
import ssgssak.ssgpointuser.domain.point.entity.*;
import ssgssak.ssgpointuser.domain.point.infrastructure.PointRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PointServiceImpl implements PointService {
    private final PointRepository pointRepository;
    private final ModelMapper modelMapper;

    /**
     * 포인트
     * 1. 유저 기존 totalPoint 조회
     * 2. 포인트 사용/적립 계산
     * 3. 포인트 생성
     * 4. 가맹점(스토어)로 적립
     * 5. 제휴사(파트너)로 적립
     * 6. 포인트 선물받기(수락) -> 포인트 2개 생성
     * 7. 포인트 전환하기
     * 8. 포인트 조회하기
     * 9. 사용가능 포인트 조회
     * 10. 기간별 적립한/사용한 포인트 계산
     * 11. 이벤트 포인트 적립
     */


    // 1. 유저 기존 totalPoint 조회
    @Override
    public Integer getTotalPoint(String uuid) {
        Optional<Point> point = pointRepository.findFirstByUserUUIDOrderByCreateAtDesc(uuid);
        if (point.isPresent()) {
            return point.get().getTotalPoint();
        } else {
            return 0;
        }
    }

    // 2. 포인트 사용/적립 계산
    @Override
    public Integer calcTotalPoint(Boolean used, Integer totalPoint, Integer updatePoint) {
        if (!used) {
            totalPoint += updatePoint;
        } else {
            totalPoint -= updatePoint;
        }
        return totalPoint;
    }

    // 3. 포인트 생성
    @Override
    public Point createPoint(CreatePointDto pointDto, String uuid) {
        Integer updateTotalPoint = calcTotalPoint(pointDto.getUsed(), getTotalPoint(uuid), pointDto.getUpdatePoint());
        pointDto = pointDto.toBuilder().totalPoint(updateTotalPoint).build();
        Point point = Point.builder().userUUID(uuid).build();
        modelMapper.map(pointDto, point);
        return point;
    }

    // 4. 가맹점(스토어)로 적립 //todo: 모든 적립 vo를 createPoint dto로 바꾸면됨
    @Override
    public PointIdOutDto pointAddStore(CreatePointDto pointDto, String uuid) {
        // 포인트 계산
        pointDto = pointDto.toBuilder().type(PointType.STORE).build();
        Point point = createPoint(pointDto, uuid);
        pointRepository.save(point);
        Long pointId = point.getId();

        return PointIdOutDto.builder().pointId(pointId).build();
    }

    // 5. 제휴사(파트너)로 적립
    @Override
    public PointIdOutDto pointAddPartner(CreatePointDto pointDto, String uuid) {
        // 포인트 계산
        pointDto = pointDto.toBuilder().type(PointType.PARTNER).build();
        Point point = createPoint(pointDto, uuid);
        pointRepository.save(point);
        Long pointId = point.getId();

        return PointIdOutDto.builder().pointId(pointId).build();
    }


    // 6. 포인트 선물받기(수락) -> 포인트 생성
    @Override
    public PointGiftAcceptResponseDto receiveGiftPoint(PointGiftAcceptRequestDto requestDto) {
        String receiverUUID = requestDto.getReceiverUUID();
        String giverUUID = requestDto.getGiverUUID();

        // giver 포인트 생성
        Integer updateGiverTotalPoint = calcTotalPoint(true, getTotalPoint(giverUUID), requestDto.getUpdatePoint());
        CreatePointDto createPointGiverDto = modelMapper.map(requestDto, CreatePointDto.class);
        createPointGiverDto = createPointGiverDto.toBuilder().used(true).uuid(giverUUID).totalPoint(updateGiverTotalPoint).build();
        Point givePoint = createPoint(createPointGiverDto, giverUUID);
        pointRepository.save(givePoint);
        Long givePointId = givePoint.getId();

        // receiver 포인트 생성
        Integer updateReceiverTotalPoint = calcTotalPoint(false, getTotalPoint(receiverUUID), requestDto.getUpdatePoint());
        CreatePointDto createPointReceiverDto = modelMapper.map(requestDto, CreatePointDto.class);
        createPointReceiverDto = createPointReceiverDto.toBuilder().used(false).uuid(receiverUUID).totalPoint(updateReceiverTotalPoint).build();
        Point receivePoint = createPoint(createPointReceiverDto, receiverUUID);
        pointRepository.save(receivePoint);
        Long receivePointId = receivePoint.getId();

        return PointGiftAcceptResponseDto.builder()
                .givePointId(givePointId)
                .receivePointId(receivePointId)
                .build();
    }

    // 7. 포인트 전환하기
    @Override
    public PointIdOutDto pointExchange(CreatePointDto pointDto, String uuid) {
        // 포인트 생성
        Integer updateTotalPoint = calcTotalPoint(pointDto.getUsed(), getTotalPoint(uuid), pointDto.getUpdatePoint());
        pointDto = pointDto.toBuilder()
                .type(PointType.EXCHANGE)
                .totalPoint(updateTotalPoint)
                .build();
        Point point = createPoint(pointDto, uuid);
        pointRepository.save(point);
        Long pointId = point.getId();

        return PointIdOutDto.builder().pointId(pointId).build();
    }

    // 8. 포인트 조회하기 todo: startDay와 endDay 타입변경하기, 컨트롤러에서
    @Override
    public PointListResponseDto pointSearch(PointListRequestDto requestDto, String uuid) {
        LocalDateTime startDay = requestDto.getStartDay();
        LocalDateTime endDay = requestDto.getEndDay();
        PointType type = requestDto.getType();
        Boolean used = requestDto.getUsed();
        log.info("sttday : "+startDay);
        log.info("endday : "+endDay);

        List<Point> pointList = null;
        // 기간이 정해져 있을 때
        if (startDay != null && endDay != null) {
            // 1. 전체 조회
            if (type == null && used == null) {
                pointList = pointRepository.findAllByUserUUIDAndCreateAtBetween(uuid, startDay, endDay);
                log.info("pointList : "+pointList);
            }
            // 2. 전체 타입을, 선택한 사용유무로 검색
            else if (type == null && used != null) {
                pointList = pointRepository.findAllByUserUUIDAndUsedAndCreateAtBetween(uuid, used, startDay, endDay);
            }
            // 3. 선택한 타입을, 전체 사용유무로 검색
            else if (type != null && used == null) {
                // 일반 타입이라면, 이벤트를 제외하고 검색한다
                if (type == PointType.GENERAL) {
                    pointList = pointRepository.findAllByUserUUIDAndTypeNotAndCreateAtBetween(uuid, PointType.EVENT, startDay, endDay);
                }
                // 이벤트 타입이라면, 이벤트만 검색한다
                else {
                    pointList = pointRepository.findAllByUserUUIDAndTypeAndCreateAtBetween(uuid, type, startDay, endDay);
                }
            }
            // 4. 선택한 타입과, 선택한 사용유무로 검색
            else {
                // 일반 타입이라면, 이벤트를 제외하고 검색한다
                if (type == PointType.GENERAL) {
                    pointList = pointRepository.findAllByUserUUIDAndTypeNotAndUsedAndCreateAtBetween(uuid, PointType.EVENT, used, startDay, endDay);
                }
                // 이벤트 타입이라면, 이벤트만 검색한다
                else {
                    pointList = pointRepository.findAllByUserUUIDAndTypeAndUsedAndCreateAtBetween(uuid, type, used, startDay, endDay);
                }
            }
        }
        // 기간 없이, 전체 기간을 조회할때 -> 일단은 선물포인트만
        else {
            // 사용유무 관계없이 전체를 검색
            if (type == PointType.GIFT && used == null) {
                pointList = pointRepository.findAllByUserUUIDAndType(uuid, type);
            }
            // 사용유무에 따라서 검색
            else if (type == PointType.GIFT && used != null) {
                pointList = pointRepository.findAllByUserUUIDAndTypeAndUsed(uuid, type, used);
            }
        }
        // 적립/사용 포인트 계산
        HashMap<String, Integer> addUsedPointList = calcAddUsedPoint(pointList);

        return PointListResponseDto.builder()
                .addTotalPoint(addUsedPointList.get("addPoint"))
                .usedTotalPoint(addUsedPointList.get("usedPoint"))
                .totalRows(pointList.size())
                .pointList(pointList)
                .build();
    }

    // 9. 사용가능 포인트 조회
    @Override
    public PointPossibleResponseDto searchPossible(String uuid) {
        //todo: 일단 totalpoint를 return해주는데, 나중에 적립예정을 빼고 보내야함
        return PointPossibleResponseDto.builder()
                .possiblePoint(getTotalPoint(uuid))
                .build();
    }

    // 10. 기간별 적립한/사용한 포인트 계산
    @Override
    public HashMap<String, Integer> calcAddUsedPoint(List<Point> pointList) {
        HashMap<String, Integer> addUsedPointList = new HashMap<>();
        Integer addPoint = pointList.stream()
                .filter(point -> point.getUsed() == false)
                .mapToInt(Point::getUpdatePoint)
                .sum();
        Integer usedPoint = pointList.stream()
                .filter(point -> point.getUsed() == true)
                .mapToInt(Point::getUpdatePoint)
                .sum();
        addUsedPointList.put("addPoint", addPoint);
        addUsedPointList.put("usedPoint", usedPoint);
        return addUsedPointList;
    }

    // 11. 이벤트 포인트 적립
    public PointIdOutDto pointAddEvent(CreatePointDto pointDto, String uuid) {
        // 포인트 계산
        pointDto = pointDto.toBuilder().type(PointType.EVENT).build();
        Point point = createPoint(pointDto, uuid);
        pointRepository.save(point);
        Long pointId = point.getId();

        return PointIdOutDto.builder().pointId(pointId).build();
    }
}
