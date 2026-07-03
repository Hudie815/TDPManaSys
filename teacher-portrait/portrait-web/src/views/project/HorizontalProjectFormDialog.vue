<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑横向项目' : '新增横向项目'"
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
          <el-form-item label="项目名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入项目名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="合作企业名称" prop="companyName">
            <el-input v-model="form.companyName" placeholder="请输入合作企业名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="合同金额(万元)" prop="contractAmount">
            <el-input-number v-model="form.contractAmount" :min="0.01" :precision="2" :step="1" placeholder="请输入合同金额" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="参与角色" prop="role">
            <el-select v-model="form.role" placeholder="请选择参与角色" style="width:100%">
              <el-option label="主持" value="主持" />
              <el-option label="参与" value="参与" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="签订日期" prop="signDate">
            <el-date-picker v-model="form.signDate" type="date" placeholder="请选择签订日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="完成日期">
            <el-date-picker v-model="form.endDate" type="date" placeholder="请选择完成日期" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="项目状态" prop="status">
            <el-select v-model="form.status" placeholder="请选择项目状态" style="width:100%">
              <el-option label="在研" value="在研" />
              <el-option label="已结题" value="已结题" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div :class="responsive.isMobile.value ? 'mobile-dialog-footer' : ''">
        <el-button @click="$emit('update:visible', false)">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createHorizontalProject, updateHorizontalProject } from '../../api/horizontalProject'
import { useResponsive } from '../../composables/useResponsive'

// 响应式布局检测
const responsive = useResponsive()

const props = defineProps({
  visible: Boolean,
  editData: Object
})

const emit = defineEmits(['update:visible', 'success'])

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  companyName: '',
  contractAmount: null,
  signDate: '',
  endDate: '',
  role: '',
  status: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  companyName: [{ required: true, message: '请输入合作企业名称', trigger: 'blur' }],
  contractAmount: [{ required: true, message: '请输入合同金额', trigger: 'blur' }],
  signDate: [{ required: true, message: '请选择签订日期', trigger: 'change' }],
  role: [{ required: true, message: '请选择参与角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        form[key] = props.editData[key] !== undefined ? props.editData[key] : (key === 'contractAmount' ? null : '')
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  form.name = ''
  form.companyName = ''
  form.contractAmount = null
  form.signDate = ''
  form.endDate = ''
  form.role = ''
  form.status = ''
  form.remark = ''
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateHorizontalProject(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createHorizontalProject(form)
      ElMessage.success('新增成功')
    }
    emit('success')
    emit('update:visible', false)
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  formRef.value?.resetFields()
  resetForm()
}
</script>

<style scoped>
/* 移动端底部按钮平分宽度 */
.mobile-dialog-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.mobile-dialog-footer :deep(.el-button) {
  flex: 1;
  margin: 0;
}
</style>
