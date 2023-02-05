package gg.rsmod.plugins.content.objs

private val CLIMB_UP_ANIMATION = 828

private val CLIMB_DOWN_ANIMATION = 827

private val climbableFields = listOf<String>("Climb", "Climb-up", "Climb-down")

for (id in 0..world.definitions.getCount(ObjectDef::class.java)) {
  /*  val objectDef = world.definitions.get(ObjectDef::class.java, id) ?: continue
    val options = objectDef.options
    for (field in climbableFields) {
        if (options.contains(field)) {
            on_obj_option(id, field) { ->
                player.queue(TaskPriority.STRONG) {

                    val gameObject = pawn.getInteractingGameObj()

                    println("object=$gameObject")

                    //climbLadder(this, gameObject, field)
                }
            }
        }
    }*/
}

/**
 * The base function for climbing
 */
fun climb(player: Player, animation: Int?, destination: Tile, vararg messages: String) {
    player.queue {
        player.lock = LockState.DELAY_ACTIONS
        player.animate(animation!!)
        this.wait(2)
        player.moveTo(destination)
        messages.forEach(player::message)
    }
}

/**
 * Handles the climbing of a ladder.
 *
 * @param player
 * The player.
 * @param object
 * The game object.
 * @param option
 * The option.
 */
fun climbLadder(it: QueueTask, `object`: GameObject, option: String) {
    val player = it.player
    var newLadder: GameObject? = null
    var animation: Int? = CLIMB_UP_ANIMATION
    val objectDef = world.definitions.get(ObjectDef::class.java, `object`.id)

    if (option.equals("climb-up", ignoreCase = true)) {
        newLadder = getTargetObject(it, `object`, false)
    } else if (option.equals("climb-down", ignoreCase = true)) {
        if (objectDef.name.equals("Trapdoor")) {
            animation = CLIMB_DOWN_ANIMATION
        }
        newLadder = getTargetObject(it, `object`, true)
    } else if (option.equals("climb", ignoreCase = true)) {
        val upperLadder = getTargetObject(it, `object`, false)
        val downLadder = getTargetObject(it, `object`, true)
        if (upperLadder == null && downLadder != null) {
            climbLadder(it, `object`, "climb-down")
            return
        }
        if (upperLadder != null && downLadder == null) {
            climbLadder(it, `object`, "climb-up")
            return
        }
        // TODO: dialogue for climbing up stairs
        // player.getDialogueManager().startDialogue(ClimbDialogue::class.java, `object`)
        return
    }
    val destination: Tile? = if (newLadder != null) getDestination(newLadder) else null
    if (newLadder == null || destination == null) {
        player.message("The ladder doesn't seem to lead to anywhere.")
        return
    }
    if (objectDef.name.startsWith("Stair")) {
        animation = null
    }
    climb(player, animation, destination)
}

fun getTargetObject(it: QueueTask, source: GameObject, down: Boolean): GameObject? {
    val p = it.player
    val world = p.world

    // the modification to apply on height in order to find the target object
    val mod = if (down) -1 else 1

    // the destination object we're looking to find
    var gameObject: GameObject? =
        world.getObject(p.tile.transform(0, if (mod == -1) mod * -6400 else 0, if (mod == -1) 0 else mod), source.type)

    val sourceDef = world.definitions.get(ObjectDef::class.java, source.id)
    val destinationDef = world.definitions.get(ObjectDef::class.java, gameObject?.id ?: -1)

    if (gameObject == null || !isLadder(destinationDef)) {
        if (gameObject != null && destinationDef.name == sourceDef.name) {
            gameObject = world.getObject(gameObject.tile.transform(0, 0, mod), source.type)
            if (gameObject != null) {
                return gameObject
            }
        }
        gameObject = findLadder(it, source.tile.transform(0, 0, mod), source.type)
        if (gameObject == null) {
            gameObject = world.getObject(source.tile.transform(0, mod * -6400, 0), source.type)
            if (gameObject == null) {
                gameObject = findLadder(it, source.tile.transform(0, mod * -6400, 0), source.type)
            }
        }
    }
    return gameObject
}

/**
 * Checks if the object is a ladder.
 *
 * @param def
 * The object's definitions.
 * @return `True` if so.
 */
fun isLadder(def: ObjectDef): Boolean {
    for (option in def.options) {
        if (option != null && option.contains("Climb")) {
            return true
        }
    }
    return def.name == "Trapdoor"
}

/**
 * Finds a ladder (by searching a 10x10 area around the given location).
 *
 * @param l
 * The location.
 * @return The ladder.
 */
fun findLadder(it: QueueTask, l: Tile, type: Int): GameObject? {
    val world = it.player.world
    for (x in -5..5) {
        for (y in -5..5) {
            val transform: Tile = l.transform(x, y, 0)
            val gameObject = world.getObject(transform, type)
            val objectDef = world.definitions.get(ObjectDef::class.java, gameObject?.id ?: -1)
            if (gameObject != null && isLadder(objectDef)) {
                return gameObject
            }
        }
    }
    return null
}

fun getDestination(`object`: GameObject): Tile? {/*    var sizeX: Int = `object`.getDefinitions().getSizeX()
        var sizeY: Int = `object`.getDefinitions().getSizeY()
        if (`object`.getRotation() % 2 !== 0) {
            val switcher = sizeX
            sizeX = sizeY
            sizeY = switcher
        }
        val dir: Direction = forWalkFlag(`object`.getDefinitions().getAccessBlockFlag(), `object`.getRotation())
        if (dir != null) {
            return getDestination(`object`, sizeX, sizeY, dir, 0)
        }
        when (`object`.getRotation()) {
            0 -> return getDestination(`object`, sizeX, sizeY, Direction.SOUTH, 0)
            1 -> return getDestination(`object`, sizeX, sizeY, Direction.EAST, 0)
            2 -> return getDestination(`object`, sizeX, sizeY, Direction.NORTH, 0)
            3 -> return getDestination(`object`, sizeX, sizeY, Direction.WEST, 0)
        }*/
    return null
}


/**
 * Gets the direction for the given walking flag.
 *
 * @param walkingFlag
 * The walking flag.
 * @param rotation
 * The rotation.
 * @return The direction, or null if the walk flag was 0.
 */
fun forWalkFlag(walkingFlag: Int, rotation: Int): Direction? {
    var walkingFlag = walkingFlag
    if (rotation != 0) {
        walkingFlag = (walkingFlag shl rotation and 0xf) + (walkingFlag shr 4 - rotation)
    }
    if (walkingFlag > 0) {
        if (walkingFlag and 0x8 == 0) {
            return Direction.WEST
        }
        if (walkingFlag and 0x2 == 0) {
            return Direction.EAST
        }
        if (walkingFlag and 0x4 == 0) {
            return Direction.SOUTH
        }
        if (walkingFlag and 0x1 == 0) {
            return Direction.NORTH
        }
    }
    return null
}