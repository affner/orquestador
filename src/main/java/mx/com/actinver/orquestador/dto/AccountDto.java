package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.entity.AccountEntity;
import mx.com.actinver.orquestador.util.MappingHelper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDto {

    private Long contractId;

    private Integer year;

    private Integer month;

    private FjXsaInfoDto xsaInfo;

    private TemplateContent version;

    public static AccountDtoBuilder builder() { return new AccountDtoBuilder();}

    public static AccountDtoBuilder builder(AccountEntity accountEntity) {
        AccountDtoBuilder builder = builder();
        builder.contractId(accountEntity.getContractId())
                .year(accountEntity.getYear())
                .month(accountEntity.getMonth())
                .xsaInfo(MappingHelper.toClass(accountEntity.getXsaInfo(),FjXsaInfoDto.class))
                .version(MappingHelper.toClass(accountEntity.getVersion(),TemplateContent.class));
        return builder;
    }

}
