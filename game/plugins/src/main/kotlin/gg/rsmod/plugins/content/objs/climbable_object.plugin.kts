package gg.rsmod.plugins.content.objs

import com.github.michaelbull.logging.InlineLogger

private val CLIMB_UP_ANIMATION = 828

private val CLIMB_DOWN_ANIMATION = 827

private val logger = InlineLogger()

/**
 * The base function for climbing
 */
fun climb(player: Player, animation: Int, destination: Tile, vararg messages: String) {
    player.queue {
        player.lock = LockState.DELAY_ACTIONS
        player.animate(animation)
        this.wait(2)
        player.moveTo(destination)
        messages.forEach(player::message)
    }
}

for (id in 0..world.definitions.getCount(ObjectDef::class.java)) {
    val options = world.definitions.get(ObjectDef::class.java, id).options
    if (options.contains("Climb-up")) {
        on_obj_option(obj = id, option = "Climb-up") {
            logger.info { "Object = ${this.pawn}" }
            // ... do code
        }
    }
}