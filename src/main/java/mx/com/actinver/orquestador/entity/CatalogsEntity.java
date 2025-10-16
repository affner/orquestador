package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import mx.com.actinver.orquestador.dto.CatalogsDto;
import mx.com.actinver.orquestador.util.ValueDeserializer;
import mx.com.actinver.orquestador.util.ValueSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class CatalogsEntity {

    @JsonProperty("FICATALOGID")
    private Long catalogId;

    @JsonProperty("FIPARENTID")
    private Long parentId;

    @JsonProperty("FCCATNAME")
    private String name;

    @JsonProperty("FCDESCRIPTION")
    private String description;

    @JsonProperty("FCVALUE")
    @JsonDeserialize(using = ValueDeserializer.class)
    private String value;

    @JsonProperty("FCVALUE2")
    @JsonDeserialize(using = ValueDeserializer.class)
    @JsonSerialize(using = ValueSerializer.class)
    private String value2;

    @JsonProperty("FISTATUS")
    private Integer status;

    @JsonProperty("FIUSERID")
    private Long userId;

    @JsonProperty("FDCREATION")
    private String creationDate;

    @JsonProperty("FDLASTUPDATE")
    private String lastUpdateDate;

    public static CatalogsEntityBuilder builder() {
        return new CatalogsEntityBuilder();
    }

    public static CatalogsEntityBuilder builder(CatalogsDto data) {
        CatalogsEntityBuilder builder = builder();

        builder.catalogId(data.getCatalogId())
                .parentId(data.getParentId())
                .name(data.getName())
                .description(data.getDescription())
                .value(data.getValue())
                .value2(data.getValue2())
                .status(data.getStatus());

        return builder;
    }

}