package com.kskb.se.http;

import com.kskb.se.base.NotNull;
import com.kskb.se.base.Nullable;

import java.util.function.Consumer;

public interface Result<R, E> {
   static <R, E> Result<R, E> of(@NotNull R value) {
      assert value != null;
      return new ResultImpl<>(value, null);
   }
   static <R, E> Result<R, E> of(@Nullable R value, @Nullable E error) {
      return new ResultImpl<>(value, error);
   }
   static <R, E> Result<R, E> error(@NotNull E error) {
      assert error != null;
      return new ResultImpl<>(null, error);
   }

   R orElse(R value);

   Result<R, E> onError(Consumer<E> action);

   <T extends E> Result<R, E> onError(Class<T> subtype, Consumer<T> action);
}

class ResultImpl<R, E> implements Result<R, E> {
   private final @Nullable R value;
   private final @Nullable E error;

   public ResultImpl(R returnValue, E error) {
      this.value = returnValue;
      this.error = error;
   }

   @Override
   public R orElse(R value) {
      return this.value != null ? this.value : value;
   }

   @Override
   public Result<R, E> onError(Consumer<E> action) {
      if (error != null)
         action.accept(error);
      return this;
   }

   @Override
   @SuppressWarnings("unchecked")
   public <T extends E> Result<R, E> onError(Class<T> subtype, Consumer<T> action) {
      if (subtype.isInstance(error))
         action.accept((T) error);
      return this;
   }

   @Override
   public String toString() {
      return value != null ? value.toString() : error.toString();
   }
}
