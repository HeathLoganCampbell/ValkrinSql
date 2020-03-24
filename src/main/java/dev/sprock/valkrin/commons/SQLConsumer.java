package dev.sprock.valkrin.commons;

import java.sql.SQLException;

public interface SQLConsumer<T> {
  void accept(T paramT) throws SQLException;
}