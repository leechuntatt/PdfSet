APP_MODULES:=mupdf


ifdef NDK_PROFILER
APP_OPTIM := debug
APP_CFLAGS := -O2
else
ifdef DEBUG
APP_OPTIM := debug
APP_CFLAGS := -DDEBUG
else
APP_OPTIM := release
endif
endif
ifdef V8_BUILD
APP_STL := stlport_static
endif
ifdef MEMENTO
APP_CFLAGS += -DMEMENTO -DMEMENTO_LEAKONLY
endif
APP_CPPFLAGS += -fexceptions
APP_ABI := all