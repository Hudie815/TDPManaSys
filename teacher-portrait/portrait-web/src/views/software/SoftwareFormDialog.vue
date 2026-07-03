<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑软著' : '新增软著'"
    :width="responsive.isMobile.value ? '90%' : '650px'"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :label-position="responsive.isMobile.value ? 'top' : 'right'"
      :label-width="responsive.isMobile.value ? '100%' : '110px'"
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="软件名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入软件名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="登记号" prop="registrationNo">
            <el-input v-model="form.registrationNo" placeholder="请输入登记号" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="版本号">
            <el-input v-model="form.version" placeholder="V1.0" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="开发完成日期" prop="devCompletionDate">
            <el-date-picker v-model="form.devCompletionDate" type="date" placeholder="请选择开发完成日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="首次发表日期">
            <el-date-picker v-model="form.firstPublishDate" type="date" placeholder="请选择首次发表日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="登记日期" prop="registrationDate">
            <el-date-picker v-model="form.registrationDate" type="date" placeholder="请选择登记日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="著作权人">
        <el-input v-model="form.copyrightOwners" placeholder="多个著作权人用分号分隔" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer" :class="{ 'mobile-footer': responsive.isMobile.value }">
        <el-button @click="$emit('update:visible', false')">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createSoftwareCopyright, updateSoftwareCopyright } from '../../api/softwareCopyright'
import { useResponsive } from '../../composables/useResponsive'

const props = defineProps({ visible: Boolean, editData: Object })
const emit = defineEmits(['update:visible', 'success'])

// 响应式布局检测
const responsive = useResponsive()

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '', registrationNo: '', version: '',
  devCompletionDate: '', firstPublishDate: '', registrationDate: '',
  copyrightOwners: '', remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入软件名称', trigger: 'blur' }],
  registrationNo: [{ required: true, message: '请输入登记号', trigger: 'blur' }],
  devCompletionDate: [{ required: true, message: '请选择开发完成日期', trigger: 'change' }],
  registrationDate: [{ required: true, message: '请选择登记日期', trigger: 'change' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        form[key] = props.editData[key] !== undefined ? props.editData[key] : ''
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  form.name = ''; form.registrationNo = ''; form.version = ''
  form.devCompletionDate = ''; form.firstPublishDate = ''; form.registrationDate = ''
  form.copyrightOwners = ''; form.remark = ''
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSoftwareCopyright(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createSoftwareCopyright(form)
      ElMessage.success('新增成功')
    }
    emit('success')
    emit('update:visible', false)
  } finally { submitting.value = false }
}

function handleClose() {
  formRef.value?.resetFields()
  resetForm()
}
</script>

<style scoped>
/* 移动端底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer.mobile-footer {
  justify-content: space-between;
}

.dialog-footer.mobile-footer :deep(.el-button) {
  flex: 1;
}
</style>