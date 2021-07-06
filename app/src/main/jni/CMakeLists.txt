cmake_minimum_required(VERSION 3.10)

include_directories(../../../../../breakpad/src)
include_directories(../../../../../breakpad/src/common/android/include)

#include_directories($(LSS_PATH))

set(SRC ../../../../../breakpad/src)

add_library(
        breakpad
        STATIC

        ${SRC}/client/linux/crash_generation/crash_generation_client.cc
        ${SRC}/client/linux/dump_writer_common/thread_info.cc
        ${SRC}/client/linux/dump_writer_common/ucontext_reader.cc
        ${SRC}/client/linux/handler/exception_handler.cc
        ${SRC}/client/linux/handler/minidump_descriptor.cc
        ${SRC}/client/linux/log/log.cc
        ${SRC}/client/linux/microdump_writer/microdump_writer.cc
        ${SRC}/client/linux/minidump_writer/linux_dumper.cc
        ${SRC}/client/linux/minidump_writer/linux_ptrace_dumper.cc
        ${SRC}/client/linux/minidump_writer/minidump_writer.cc
        ${SRC}/client/minidump_file_writer.cc
        ${SRC}/common/convert_UTF.cc
        ${SRC}/common/md5.cc
        ${SRC}/common/string_conversion.cc
        ${SRC}/common/linux/breakpad_getcontext.S
        ${SRC}/common/linux/elfutils.cc
        ${SRC}/common/linux/file_id.cc
        ${SRC}/common/linux/guid_creator.cc
        ${SRC}/common/linux/linux_libc_support.cc
        ${SRC}/common/linux/memory_mapped_file.cc
        ${SRC}/common/linux/safe_readlink.cc
)

add_library(
        test_breakpad
        SHARED
        test_breakpad.cpp
)

find_library(
        log_lib
        log
)

target_link_libraries(
        test_breakpad
        log
#        breakpad
)