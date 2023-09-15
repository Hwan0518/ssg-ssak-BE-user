package ssgssak.ssgpointuser.domain.club.application;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ssgssak.ssgpointuser.domain.club.dto.CarClubDto;
import ssgssak.ssgpointuser.domain.club.entity.CarClub;
import ssgssak.ssgpointuser.domain.club.entity.ClubList;
import ssgssak.ssgpointuser.domain.club.infrastructure.CarClubRepository;
import ssgssak.ssgpointuser.domain.club.infrastructure.ClubListRepository;

@Service
@RequiredArgsConstructor
public class CarClubServiceImpl implements ClubService<CarClubDto>{
    private final CarClubRepository carClubRepository;
    private final ClubListRepository clubListRepository;
    private final ModelMapper modelMapper;

    // 유저 클럽 정보 생성
    @Override
    public void createClubUser(CarClubDto createDto, String uuid) {
        // 가입되어있는지 확인(중복 가입 방지)
        ClubList clubList = clubListRepository.findByUuid(uuid);
        if(clubList.getCarClub() != null){
            throw new IllegalArgumentException("이미 클럽에 가입되어 있습니다.");
        }
        CarClub carClub = CarClub.builder()
                .region(createDto.getRegion())
                .firstNumber(createDto.getFirstNumber())
                .middleNumber(createDto.getMiddleNumber())
                .lastNumber(createDto.getLastNumber())
                .agreementMandatory(createDto.getAgreementMandatory())
                .agreementOptional(createDto.getAgreementOptional())
                .build();
        carClubRepository.save(carClub);
        clubList.updateCarClubInfo(carClub);
        clubListRepository.save(clubList);
    }
    // 유저 클럽 정보 가져오기
    @Override
    public CarClubDto getClubUser(String uuid) {
        ClubList clubList = clubListRepository.findByUuid(uuid);
        CarClub carClub = carClubRepository.findById(clubList.getCarClub().getId())
                .orElseThrow(() -> new IllegalArgumentException("차량 클럽 정보가 없습니다."));
        return modelMapper.map(carClub, CarClubDto.class);
    }
    // 유저 클럽 정보 수정/삭제
    @Override
    public void updateClubUser(CarClubDto clubDto, String uuid) {
        ClubList clubList = clubListRepository.findByUuid(uuid);
        CarClub carClub = carClubRepository.findById(clubList.getCarClub().getId())
                .orElseThrow(() -> new IllegalArgumentException("차량 클럽 정보가 없습니다."));
        // 약관 미동의시 클럽 정보 삭제
        if(!clubDto.getAgreementMandatory()){
            clubList.updateCarClubInfo(null);
            clubListRepository.save(clubList);
            carClubRepository.delete(carClub);
        }
        else {
            CarClub updatedCarClub = carClub.toBuilder()
                    .region(clubDto.getRegion())
                    .firstNumber(clubDto.getFirstNumber())
                    .middleNumber(clubDto.getMiddleNumber())
                    .lastNumber(clubDto.getLastNumber())
                    .agreementMandatory(clubDto.getAgreementMandatory())
                    .agreementOptional(clubDto.getAgreementOptional())
                    .build();
            carClubRepository.save(updatedCarClub);
        }
    }
}
