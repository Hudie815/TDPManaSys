<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑竞赛记录' : '新增竞赛记录'"
    :width="responsive.isMobile.value ? '90%' : '650px'"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
    @closed="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" :label-position="responsive.isMobile.value ? 'top' : 'right'" :label-width="responsive.isMobile.value ? '100%' : '100px'">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="竞赛名称" prop="name">
            <el-input v-model="form.name" placeholder="请输入竞赛名称" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="主办单位">
            <el-input v-model="form.organizer" placeholder="请输入主办单位" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="参赛时间" prop="competitionDate">
            <el-date-picker v-model="form.competitionDate" type="date" placeholder="请选择参赛时间" value-format="YYYY-MM-DD" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="获奖级别" prop="awardLevel">
            <el-select v-model="form.awardLevel" placeholder="请选择获奖级别" style="width:100%">
              <el-option label="国家级" value="国家级" />
              <el-option label="省级" value="省级" />
              <el-option label="校级" value="校级" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="获奖等级" prop="awardGrade">
            <el-select v-model="form.awardGrade" placeholder="请选择获奖等级" style="width:100%">
              <el-option label="特等奖" value="特等奖" />
              <el-option label="一等奖" value="一等奖" />
              <el-option label="二等奖" value="二等奖" />
              <el-option label="三等奖" value="三等奖" />
              <el-option label="优秀奖" value="优秀奖" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="指导排名">
            <el-input-number v-model="form.guideRank" :min="1" :step="1" placeholder="指导排名" style="width:100%" />
          </el-form-item>
        </el-col>
        <el-col :span="responsive.isMobile.value ? 24 : 12">
          <el-form-item label="证书编号">
            <el-input v-model="form.certificateNo" placeholder="请输入证书编号" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="学生团队">
        <el-input v-model="form.studentTeam" placeholder="多个学生用分号分隔" />
      </el-form-item>
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
import { createCompetition, updateCompetition } from '../../api/competition'
import { useResponsive } from '../../composables/useResponsive'

// 响应式布局检测
const responsive = useResponsive()

const props = defineProps({ visible: Boolean, editData: Object })
const emit = defineEmits(['update:visible', 'success'])

const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '', organizer: '', competitionDate: '',
  studentTeam: '', awardLevel: '', awardGrade: '',
  guideRank: 1, certificateNo: '', remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入竞赛名称', trigger: 'blur' }],
  competitionDate: [{ required: true, message: '请选择参赛时间', trigger: 'change' }],
  awardLevel: [{ required: true, message: '请选择获奖级别', trigger: 'change' }],
  awardGrade: [{ required: true, message: '请选择获奖等级', trigger: 'change' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.editData) {
      isEdit.value = true
      Object.keys(form).forEach(key => {
        const v = props.editData[key]
        form[key] = key === 'guideRank' ? (v || 1) : (v !== undefined ? v : '')
      })
    } else {
      isEdit.value = false
      resetForm()
    }
  }
})

function resetForm() {
  form.name = ''; form.organizer = ''; form.competitionDate = ''
  form.studentTeam = ''; form.awardLevel = ''; form.awardGrade = ''
  form.guideRank = 1; form.certificateNo = ''; form.remark = ''
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateCompetition(props.editData.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createCompetition(form)
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
