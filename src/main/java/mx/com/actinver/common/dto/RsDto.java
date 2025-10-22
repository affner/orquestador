package mx.com.actinver.common.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class RsDto<T> {

	private String timestamp;

	private String message;

	private List<T> content;

	private List<?> complement;

	private Pagination pagination;

	@Data
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class Pagination {

		private Long totalPages;

		private Long totalElements;

		private Long pageSize;

		public static PaginationBuilder builder() {
			return new PaginationBuilder();
		}

		public static PaginationBuilder builder(Long totalPages, Long totalElements, Long pageSize) {
			PaginationBuilder builder = builder();

			builder.totalPages(totalPages).totalElements(totalElements).pageSize(pageSize);

			return builder;
		}

	}

	public static <T> RsDtoBuilder<T> builder() {
		RsDtoBuilder<T> builder = new RsDtoBuilder<T>();

		builder.timestamp(LocalDateTime.now().toString()).content(new ArrayList<>());

		return builder;
	}

	public static RsDtoBuilder<String> builder(String message) {
		RsDtoBuilder<String> builder = builder();

		builder.message(message);

		return builder;
	}

	public static <T> RsDtoBuilder<T> builder(T e) {
		RsDtoBuilder<T> builder = builder();
		
		builder.content.add(e);

		return builder;
	}

	public static <T> RsDtoBuilder<T> builder(List<T> content) {
		RsDtoBuilder<T> builder = builder();

		builder.content(content);

		return builder;
	}

	public static <T> RsDtoBuilder<T> builder(Pagination pagination, List<T> content) {
		RsDtoBuilder<T> builder = builder(content);

		builder.pagination(pagination);

		return builder;
	}

	public <R> RsDto<R> map(Function<T, R> converter) {
		List<R> ct = new ArrayList<>();

		if (Objects.nonNull(content)) {
			ct = content.stream().map(converter::apply).collect(Collectors.toList());
		}

		return RsDto.builder(pagination, ct).build();
	}

	@JsonIgnore
	public T getFirst() {
		T first = null;

		if (Objects.nonNull(content) && !content.isEmpty()) {
			first = content.stream().findFirst().orElse(first);
		}

		return first;
	}

	/**
	 * Permite comprobar si el contenido es nulo o vacio.
	 *
	 * @return <code>True</code> si y solo si el contenido es nulo o vacio.
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return Objects.isNull(content) || content.isEmpty();
	}

	/**
	 * Permite comprobar si el contenido no es nulo o no es vacio.
	 *
	 * @return <code>True</code> si y solo si el contenido no es nulo o no es vacio.
	 */
	@JsonIgnore
	public boolean isNotEmpty() {
		return !isEmpty();
	}
}
