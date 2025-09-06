package vn.vnsky.bcss.admin.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.constant.CacheKey;
import vn.vnsky.bcss.admin.dto.ParamDTO;
import vn.vnsky.bcss.admin.entity.ParamEntity;
import vn.vnsky.bcss.admin.repository.ParamRepository;
import vn.vnsky.bcss.admin.service.ParamService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParamServiceImpl implements ParamService {

    private static final List<String> excludeCodes = new ArrayList<>();

    static {
        excludeCodes.add("ENABLE_ACTIVE");
        excludeCodes.add("PASSWORD_ACTIVE");
        excludeCodes.add("URL");
        excludeCodes.add("OTP");
        excludeCodes.add("TOKEN");
        excludeCodes.add("SERVICE");
        excludeCodes.add("ORDER_RESULT");
    }

    private final ParamRepository paramRepository;

    @Autowired
    public ParamServiceImpl(ParamRepository paramRepository) {
        this.paramRepository = paramRepository;
    }

    @Cacheable(cacheNames = CacheKey.PARAM_PREFIX, key = "#code")
    public List<ParamDTO> getParamByCode(String code, String language) {
        String keySearch = code;
        code = this.getKey(code, language);
        log.info("PUT KEY REDIS " + code);
        return paramRepository.getParamByParamCode(keySearch, AuthConstants.ModelStatus.ACTIVE, language);
    }

    @Cacheable(cacheNames = CacheKey.PARAM_PREFIX, key = "#redisKey")
    public Map<String, List<ParamDTO>> getMultipleParams(String language, String redisKey) {
        return paramRepository.getParamEntityByCodeNotIn(excludeCodes, AuthConstants.ModelStatus.ACTIVE, language)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                ParamEntity::getCode,
                                Collectors.mapping(
                                        p -> new ParamDTO(p.getDescription(), p.getValue()),
                                        Collectors.toList())
                        )
                );
    }

    public String getKey(String key, String languageCode) {
        if ("vi-VN".equalsIgnoreCase(languageCode)) {
            key = key + "_VI";
        } else key = key + "_EN";
        return key;
    }

    @Override
    public List<ParamDTO> getSystemUrls() {
        return this.paramRepository.findByParamCode("API_URL")
                .stream()
                .map(t -> new ParamDTO(t.getDescription(), t.getValue()))
                .toList();
    }
}
