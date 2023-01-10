package unicorn

import (
	"unsafe"
)

// #include <unicorn/unicorn.h>
// #include <unicorn/x86.h>
import "C"

type X86Mmr struct {
	Selector uint16
	Base     uint64
	Limit    uint32
	Flags    uint32
}

func (u *uc) RegWriteMmr(reg int, value *X86Mmr) error {
	var val C.uc_x86_mmr
	val.selector = C.uint16_t(value.Selector)
	val.base = C.uint64_t(value.Base)
	val.limit = C.uint32_t(value.Limit)
	val.flags = C.uint32_t(value.Flags)
	ucerr := C.uc_reg_write(u.handle, C.int(reg), unsafe.Pointer(&val))
	return errReturn(ucerr)
}

func (u *uc) RegReadMmr(reg int) (*X86Mmr, error) {
	var val C.uc_x86_mmr
	ucerr := C.uc_reg_read(u.handle, C.int(reg), unsafe.Pointer(&val))
	ret := &X86Mmr{
		Selector: uint16(val.selector),
		Base:     uint64(val.base),
		Limit:    uint32(val.limit),
		Flags:    uint32(val.flags),
	}
	return ret, errReturn(ucerr)
}

func (u *uc) RegWriteX86Msr(reg uint64, val uint64) error {
	msr := C.uc_x86_msr{
		rid:   C.uint32_t(reg),
		value: C.uint64_t(val),
	}
	return errReturn(C.uc_reg_write(u.handle, X86_REG_MSR, unsafe.Pointer(&msr)))
}

func (u *uc) RegReadX86Msr(reg uint64) (uint64, error) {
	msr := C.uc_x86_msr{
		rid: C.uint32_t(reg),
	}
	ucerr := C.uc_reg_read(u.handle, X86_REG_MSR, unsafe.Pointer(&msr))
	return uint64(msr.value), errReturn(ucerr)
}
