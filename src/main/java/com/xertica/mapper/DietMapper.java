package com.xertica.mapper;

import com.xertica.dto.DietDailyTargetDTO;
import com.xertica.dto.DietViewDTO;
import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DietMapper {

    DietMapper INSTANCE = Mappers.getMapper(DietMapper.class);

    @Mapping(source = "dailyTargets", target = "dailyTargets")
    DietViewDTO dietToDietViewDTO(Diet diet);

    DietDailyTargetDTO dietDailyTargetToDTO(DietDailyTarget dailyTarget);
}