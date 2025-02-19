import List from '@ohos.util.List'
import systemTime from '@ohos.systemTime'
import systemDateTime from '@ohos.systemDateTime'

class UUID {
  value: string

  constructor(value: string) {
    this.value = value
  }

  static random(): UUID {
    return new UUID(systemDateTime.getTime().toString())
  }
}

interface TabAction {
  action: string
  iconRes: Resource
  isVisible: boolean
  actionId: UUID
  targetRoute: string | null
}

export interface NaviTabItem {
  name: string
  routeName: string
  iconRes: Resource
  showDescription: boolean
  description: string
  isSelected: boolean
  actions: List<TabAction>
}

export default class SimpleNaviTabItem implements NaviTabItem {
  name: string
  routeName: string
  iconRes: Resource
  showDescription: boolean
  description: string
  isSelected: boolean
  actions: List<TabAction>

  constructor(
    name: string,
    routeName: string,
    iconRes: Resource,
    showDescription: boolean = false,
    description: string = "",
    isSelected: boolean = false,
    actions: List<TabAction> = new List()
  ) {
    this.routeName = routeName
    this.iconRes = iconRes
    this.showDescription = showDescription
    this.name = name
    this.description = description
    this.isSelected = isSelected
    this.actions = actions
  }
}

export class SampleTabAction implements TabAction {
  action: string
  iconRes: Resource
  actionId: UUID
  isVisible: boolean
  targetRoute: string | null
  name: string | null
  description: string | null

  constructor(
    action: string,
    iconRes: Resource,
    actionId: UUID = UUID.random(),
    isVisible: boolean = true,
    targetRoute: string | null = null,
    name: string | null = null,
    description: string | null = null
  ) {
    this.action = action
    this.iconRes = iconRes
    this.isVisible = isVisible
    this.actionId = actionId
    this.targetRoute = targetRoute
    this.name = name
    this.description = description
  }
}