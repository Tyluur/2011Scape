package gg.rsmod.plugins.content.objs

private val CLIMB_UP_ANIMATION = 828

private val CLIMB_DOWN_ANIMATION = 827

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
