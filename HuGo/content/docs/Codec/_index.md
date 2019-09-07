
# commons-codec

## Maven 依赖

``` xml
<dependency>
   <groupId>commons-codec</groupId>
   <artifactId>commons-codec</artifactId>
   <version>1.11</version>
</dependency>
```

## 简介

Codec 是一个简单的工具包，用来**编码和解码文本和二进制数据**。

## 二进制编码器

- `Base32`	Provides Base32 encoding and decoding as defined by RFC 4648
- `Base32InputStream`	Provides Base32 encoding and decoding in a streaming fashion (unlimited size).
- **`Base64`**	提供由RFC 2045定义的Base64编码和解码
- `Base64InputStream`	Provides Base64 encoding and decoding in a streaming fashion (unlimited size).
- `BinaryCodec`	Conrts between byte arrays and strings of "0"s and "1"s.
- **`Hex`**: byte[] 和 字符串 互转 工具类

## 摘要编码器

- **`DigestUtils`**	简化常见的MessageDigest操作，并提供GNU libc crypt(3) 兼容的密码散列函数

## Language Encoders

- `Caverphone 1.0`	Encodes a string into a Caverphone 1.0 value.
- `Caverphone 2.0`	Encodes a string into a Caverphone 2.0 value.
- `Cologne Phonetic`	Encodes a string into a Cologne Phonetic value.
- `Double Metaphone`	Encodes a string into a double metaphone value.
- `Metaphone`	Encodes a string into a Metaphone value.
- `Refined Soundex`	Encodes a string into a Refined Soundex value.
- `Soundex`	Encodes a string into a Soundex value.


## Network Encoders

- `BCodec`	Identical to the Base64 encoding defined by RFC 1521 and allows a character set to be specified.
- `QCodec`	Similar to the Quoted-Printable content-transfer-encoding defined in RFC 1521 and designed to allow text containing mostly ASCII characters to be decipherable on an ASCII terminal without decoding.
- `QuotedPrintableCodec`	Codec for the Quoted-Printable section of RFC 1521 .
- `URLCodec`	Implements the www-form-urlencoded encoding scheme, also misleadingly known as URL encoding.


## Read More

- [commons-codec 用户指南](http://commons.apache.org/proper/commons-codec/userguide.html)