package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.dto.AccountDto;
import mx.com.actinver.orquestador.util.MappingHelper;
import mx.com.actinver.orquestador.util.ValueDeserializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountEntity {

    @JsonProperty("FICONTRATO")
    private Long contractId;

    @JsonProperty("FIYEAR")
    private Integer year;

    @JsonProperty("FIMONTH")
    private Integer month;

    @JsonProperty("FJXSAINFO")
    @JsonDeserialize(using = ValueDeserializer.class)
    private String xsaInfo;

    @JsonProperty("FJVERSION")
    @JsonDeserialize(using = ValueDeserializer.class)
    private String version;

    public static AccountEntityBuilder builder() { return new AccountEntityBuilder();}

    public static AccountEntityBuilder builder(AccountDto accountDto) {
        AccountEntityBuilder builder = builder();
        builder.contractId(accountDto.getContractId())
                .year(accountDto.getYear())
                .month(accountDto.getMonth())
                .xsaInfo(MappingHelper.toJson(accountDto.getXsaInfo()))
                .version(MappingHelper.toJson(accountDto.getVersion()));
        return builder;
    }

}
