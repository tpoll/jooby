/*
 * Jooby https://jooby.io
 * Apache License Version 2.0 https://jooby.io/LICENSE.txt
 * Copyright 2014 Edgar Espina
 */
package io.jooby.buffer;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Iterator;
import java.util.function.IntPredicate;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Basic abstraction over byte buffers.
 *
 * <p>{@code DataBuffer}s has a separate {@linkplain #readPosition() read} and {@linkplain
 * #writePosition() write} position, as opposed to {@code ByteBuffer}'s single {@linkplain
 * ByteBuffer#position() position}. As such, the {@code DataBuffer} does not require a {@linkplain
 * ByteBuffer#flip() flip} to read after writing. In general, the following invariant holds for the
 * read and write positions, and the capacity:
 *
 * <blockquote>
 *
 * {@code 0} {@code <=} <i>readPosition</i> {@code <=} <i>writePosition</i> {@code <=}
 * <i>capacity</i>
 *
 * </blockquote>
 *
 * <p>The {@linkplain #capacity() capacity} of a {@code DataBuffer} is expanded on demand, similar
 * to {@code StringBuilder}.
 *
 * <p>The main purpose of the {@code DataBuffer} abstraction is to provide a convenient wrapper
 * around {@link ByteBuffer} which is similar to Netty's {@link io.netty.buffer.ByteBuf} but can
 * also be used on non-Netty platforms (i.e. Servlet containers).
 *
 * @author Arjen Poutsma
 * @author Brian Clozel
 * @since 5.0
 * @see DataBufferFactory
 */
public interface DataBuffer {

  /**
   * Return the {@link DataBufferFactory} that created this buffer.
   *
   * @return the creating buffer factory
   */
  DataBufferFactory factory();

  /**
   * Return the index of the first byte in this buffer that matches the given predicate.
   *
   * @param predicate the predicate to match
   * @param fromIndex the index to start the search from
   * @return the index of the first byte that matches {@code predicate}; or {@code -1} if none match
   */
  int indexOf(IntPredicate predicate, int fromIndex);

  /**
   * Return the index of the last byte in this buffer that matches the given predicate.
   *
   * @param predicate the predicate to match
   * @param fromIndex the index to start the search from
   * @return the index of the last byte that matches {@code predicate}; or {@code -1} if none match
   */
  int lastIndexOf(IntPredicate predicate, int fromIndex);

  /**
   * Return the number of bytes that can be read from this data buffer.
   *
   * @return the readable byte count
   */
  int readableByteCount();

  /**
   * Return the number of bytes that can be written to this data buffer.
   *
   * @return the writable byte count
   * @since 5.0.1
   */
  int writableByteCount();

  /**
   * Return the number of bytes that this buffer can contain.
   *
   * @return the capacity
   * @since 5.0.1
   */
  int capacity();

  /**
   * Ensure that the current buffer has enough {@link #writableByteCount()} to write the amount of
   * data given as an argument. If not, the missing capacity will be added to the buffer.
   *
   * @param capacity the writable capacity to check for
   * @return this buffer
   * @since 6.0
   */
  DataBuffer ensureWritable(int capacity);

  /**
   * Return the position from which this buffer will read.
   *
   * @return the read position
   * @since 5.0.1
   */
  int readPosition();

  /**
   * Set the position from which this buffer will read.
   *
   * @param readPosition the new read position
   * @return this buffer
   * @throws IndexOutOfBoundsException if {@code readPosition} is smaller than 0 or greater than
   *     {@link #writePosition()}
   * @since 5.0.1
   */
  DataBuffer readPosition(int readPosition);

  /**
   * Return the position to which this buffer will write.
   *
   * @return the write position
   * @since 5.0.1
   */
  int writePosition();

  /**
   * Set the position to which this buffer will write.
   *
   * @param writePosition the new write position
   * @return this buffer
   * @throws IndexOutOfBoundsException if {@code writePosition} is smaller than {@link
   *     #readPosition()} or greater than {@link #capacity()}
   * @since 5.0.1
   */
  DataBuffer writePosition(int writePosition);

  /**
   * Read a single byte at the given index from this data buffer.
   *
   * @param index the index at which the byte will be read
   * @return the byte at the given index
   * @throws IndexOutOfBoundsException when {@code index} is out of bounds
   * @since 5.0.4
   */
  byte getByte(int index);

  /**
   * Read a single byte from the current reading position from this data buffer.
   *
   * @return the byte at this buffer's current reading position
   */
  byte read();

  /**
   * Read this buffer's data into the specified destination, starting at the current reading
   * position of this buffer.
   *
   * @param destination the array into which the bytes are to be written
   * @return this buffer
   */
  DataBuffer read(byte[] destination);

  /**
   * Read at most {@code length} bytes of this buffer into the specified destination, starting at
   * the current reading position of this buffer.
   *
   * @param destination the array into which the bytes are to be written
   * @param offset the index within {@code destination} of the first byte to be written
   * @param length the maximum number of bytes to be written in {@code destination}
   * @return this buffer
   */
  DataBuffer read(byte[] destination, int offset, int length);

  /**
   * Write a single byte into this buffer at the current writing position.
   *
   * @param b the byte to be written
   * @return this buffer
   */
  DataBuffer write(byte b);

  /**
   * Write the given source into this buffer, starting at the current writing position of this
   * buffer.
   *
   * @param source the bytes to be written into this buffer
   * @return this buffer
   */
  DataBuffer write(byte[] source);

  /**
   * Write at most {@code length} bytes of the given source into this buffer, starting at the
   * current writing position of this buffer.
   *
   * @param source the bytes to be written into this buffer
   * @param offset the index within {@code source} to start writing from
   * @param length the maximum number of bytes to be written from {@code source}
   * @return this buffer
   */
  DataBuffer write(byte[] source, int offset, int length);

  /**
   * Write one or more {@code DataBuffer}s to this buffer, starting at the current writing position.
   * It is the responsibility of the caller to {@linkplain DataBufferUtils#release(DataBuffer)
   * release} the given data buffers.
   *
   * @param buffers the byte buffers to write into this buffer
   * @return this buffer
   */
  DataBuffer write(DataBuffer... buffers);

  /**
   * Write one or more {@link ByteBuffer} to this buffer, starting at the current writing position.
   *
   * @param buffers the byte buffers to write into this buffer
   * @return this buffer
   */
  DataBuffer write(ByteBuffer... buffers);

  /**
   * Write the given {@code CharSequence} using the given {@code Charset}, starting at the current
   * writing position.
   *
   * @param charSequence the char sequence to write into this buffer
   * @param charset the charset to encode the char sequence with
   * @return this buffer
   * @since 5.1.4
   */
  default DataBuffer write(CharSequence charSequence, Charset charset) {
    Assert.notNull(charSequence, "CharSequence must not be null");
    Assert.notNull(charset, "Charset must not be null");
    if (!charSequence.isEmpty()) {
      CharsetEncoder encoder =
          charset
              .newEncoder()
              .onMalformedInput(CodingErrorAction.REPLACE)
              .onUnmappableCharacter(CodingErrorAction.REPLACE);
      CharBuffer src = CharBuffer.wrap(charSequence);
      int averageSize = (int) Math.ceil(src.remaining() * encoder.averageBytesPerChar());
      ensureWritable(averageSize);
      while (true) {
        CoderResult cr;
        if (src.hasRemaining()) {
          try (ByteBufferIterator iterator = writableByteBuffers()) {
            Assert.state(iterator.hasNext(), "No ByteBuffer available");
            ByteBuffer dest = iterator.next();
            cr = encoder.encode(src, dest, true);
            if (cr.isUnderflow()) {
              cr = encoder.flush(dest);
            }
            writePosition(writePosition() + dest.position());
          }
        } else {
          cr = CoderResult.UNDERFLOW;
        }
        if (cr.isUnderflow()) {
          break;
        } else if (cr.isOverflow()) {
          int maxSize = (int) Math.ceil(src.remaining() * encoder.maxBytesPerChar());
          ensureWritable(maxSize);
        }
      }
    }
    return this;
  }

  /**
   * Splits this data buffer into two at the given index.
   *
   * <p>Data that precedes the {@code index} will be returned in a new buffer, while this buffer
   * will contain data that follows after {@code index}. Memory between the two buffers is shared.
   *
   * <p>The {@linkplain #readPosition() read} and {@linkplain #writePosition() write} position of
   * the returned buffer are truncated to fit within the buffers {@linkplain #capacity() capacity}
   * if necessary. The positions of this buffer are set to {@code 0} if they are smaller than {@code
   * index}.
   *
   * @param index the index at which it should be split
   * @return a new data buffer, containing the bytes from index {@code 0} to {@code index}
   * @since 6.0
   */
  DataBuffer split(int index);

  /**
   * Copies this entire data buffer into the given destination {@code ByteBuffer}, beginning at the
   * current {@linkplain #readPosition() reading position}, and the current {@linkplain
   * ByteBuffer#position() position} of destination byte buffer.
   *
   * @param dest the destination byte buffer
   * @since 6.0.5
   */
  default void toByteBuffer(ByteBuffer dest) {
    toByteBuffer(readPosition(), dest, dest.position(), readableByteCount());
  }

  /**
   * Copies the given length from this data buffer into the given destination {@code ByteBuffer},
   * beginning at the given source position, and the given destination position in the destination
   * byte buffer.
   *
   * @param srcPos the position of this data buffer from where copying should start
   * @param dest the destination byte buffer
   * @param destPos the position in {@code dest} to where copying should start
   * @param length the amount of data to copy
   * @since 6.0.5
   */
  void toByteBuffer(int srcPos, ByteBuffer dest, int destPos, int length);

  /**
   * Returns a closeable iterator over each {@link ByteBuffer} in this data buffer that can be read.
   * However, the byte buffers provided can only be used during the iteration.
   *
   * <p><b>Note</b> that the returned iterator must be used in a try-with-resources clause or
   * explicitly {@linkplain ByteBufferIterator#close() closed}.
   *
   * @return a closeable iterator over the readable byte buffers contained in this data buffer
   * @since 6.0.5
   */
  ByteBufferIterator readableByteBuffers();

  /**
   * Returns a closeable iterator over each {@link ByteBuffer} in this data buffer that can be
   * written to. The byte buffers provided can only be used during the iteration.
   *
   * <p><b>Note</b> that the returned iterator must be used in a try-with-resources clause or
   * explicitly {@linkplain ByteBufferIterator#close() closed}.
   *
   * @return a closeable iterator over the writable byte buffers contained in this data buffer
   * @since 6.0.5
   */
  ByteBufferIterator writableByteBuffers();

  /**
   * Expose this buffer's data as an {@link InputStream}. Both data and read position are shared
   * between the returned stream and this data buffer. The underlying buffer will
   * <strong>not</strong> be {@linkplain DataBufferUtils#release(DataBuffer) released} when the
   * input stream is {@linkplain InputStream#close() closed}.
   *
   * @return this data buffer as an input stream
   * @see #asInputStream(boolean)
   */
  default InputStream asInputStream() {
    return new DataBufferInputStream(this, false);
  }

  /**
   * Expose this buffer's data as an {@link InputStream}. Both data and read position are shared
   * between the returned stream and this data buffer.
   *
   * @param releaseOnClose whether the underlying buffer will be {@linkplain
   *     DataBufferUtils#release(DataBuffer) released} when the input stream is {@linkplain
   *     InputStream#close() closed}.
   * @return this data buffer as an input stream
   * @since 5.0.4
   */
  default InputStream asInputStream(boolean releaseOnClose) {
    return new DataBufferInputStream(this, releaseOnClose);
  }

  /**
   * Expose this buffer's data as an {@link OutputStream}. Both data and write position are shared
   * between the returned stream and this data buffer.
   *
   * @return this data buffer as an output stream
   */
  default OutputStream asOutputStream() {
    return new DataBufferOutputStream(this);
  }

  /**
   * Expose this buffer's data as an {@link Writer}. Both data and write position are shared between
   * the returned stream and this data buffer. Uses <code>UTF-8</code> charset.
   *
   * @return this data buffer as an output stream
   */
  default Writer asWriter() {
    return asWriter(StandardCharsets.UTF_8);
  }

  /**
   * Expose this buffer's data as an {@link Writer}. Both data and write position are shared between
   * the returned stream and this data buffer.
   *
   * @param charset Charset to use.
   * @return this data buffer as an output stream
   */
  default Writer asWriter(@NonNull Charset charset) {
    return new DataBufferWriter(this, charset);
  }

  /**
   * Return this buffer's data a String using the specified charset. Default implementation
   * delegates to {@code toString(readPosition(), readableByteCount(), charset)}.
   *
   * @param charset the character set to use
   * @return a string representation of all this buffers data
   * @since 5.2
   */
  default String toString(Charset charset) {
    Assert.notNull(charset, "Charset must not be null");
    return toString(readPosition(), readableByteCount(), charset);
  }

  /**
   * Return a part of this buffer's data as a String using the specified charset.
   *
   * @param index the index at which to start the string
   * @param length the number of bytes to use for the string
   * @param charset the charset to use
   * @return a string representation of a part of this buffers data
   * @since 5.2
   */
  String toString(int index, int length, Charset charset);

  /**
   * Clears this buffer. The position is set to zero, the limit is set to the capacity, and the mark
   * is discarded.
   *
   * <p>This method does not actually erase the data in the buffer, but it is named as if it did
   * because it will most often be used in situations in which that might as well be the case.
   *
   * @return This buffer
   */
  DataBuffer clear();

  /**
   * A dedicated iterator type that ensures the lifecycle of iterated {@link ByteBuffer} elements.
   * This iterator must be used in a try-with-resources clause or explicitly {@linkplain #close()
   * closed}.
   *
   * @since 6.0.5
   * @see DataBuffer#readableByteBuffers()
   * @see DataBuffer#writableByteBuffers()
   */
  interface ByteBufferIterator extends Iterator<ByteBuffer>, Closeable {

    @Override
    void close();
  }
}