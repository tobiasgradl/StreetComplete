package de.westnordost.streetcomplete.data.urlconfig

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlayController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderController
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeController

/** Configure (quest preset, selected overlay) through an URL */
class UrlConfigController(
    private val questTypeRegistry: QuestTypeRegistry,
    private val overlayRegistry: OverlayRegistry,
    private val selectedOverlayController: SelectedOverlayController,
    private val questPresetsController: QuestPresetsController,
    private val visibleQuestTypeController: VisibleQuestTypeController,
    private val questTypeOrderController: QuestTypeOrderController
) {
    fun apply(url: String): Boolean {
        val config = parseConfigUrl(url, questTypeRegistry, overlayRegistry) ?: return false

        // TODO what if preset by that name already exists?
        val presetId = questPresetsController.add(config.presetName)

        val questTypes = questTypeRegistry.associateWith { it in config.questTypes }
        visibleQuestTypeController.setVisibilities(questTypes, presetId)
        questTypeOrderController.setOrders(config.questTypeOrders, presetId)

        // set the current quest prest + overlay last, so the above do not trigger updates
        questPresetsController.selectedId = presetId
        selectedOverlayController.selectedOverlay = config.overlay

        return true
    }

    fun create(presetId: Long): String {
        val urlConfig = UrlConfig(
            presetName = questPresetsController.getName(presetId) ?: "Default",
            questTypes = visibleQuestTypeController.getVisible(presetId),
            questTypeOrders = questTypeOrderController.getOrders(presetId),
            overlay = selectedOverlayController.selectedOverlay
        )
        return createConfigUrl(urlConfig, questTypeRegistry, overlayRegistry)
    }
}
