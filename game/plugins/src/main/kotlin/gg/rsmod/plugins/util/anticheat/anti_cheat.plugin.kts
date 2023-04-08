package gg.rsmod.plugins.util.anticheat

import gg.rsmod.plugins.content.combat.isBeingAttacked
import java.util.*
import gg.rsmod.plugins.content.drops.DropTableFactory
import gg.rsmod.plugins.content.drops.DropTableType
import gg.rsmod.plugins.content.drops.global.Rare
import gg.rsmod.util.Misc

/**
 * @author Alycia <https://github.com/alycii>
 */

val TIMER = TimerKey(persistenceKey = "anti_cheat", tickOffline = false, resetOnDeath = false, tickForward = false, removeOnZero = true)
val LOGOUT_TIMER = TimerKey()

val range = 3000..12000
on_login {
    if(!player.timers.has(TIMER)) {
        player.timers[TIMER] = world.random(range)
    }
}

// Create a synchronized map using the `synchronizedMap` method
val timers: MutableMap<TimerKey, Int> = Collections.synchronizedMap(HashMap())

// The `on_timer` function is called every time the `TIMER` timer fires
on_timer(TIMER) {
    // Create a copy of the `timers` collection using the `synchronized` block to ensure thread safety
    synchronized(timers) {
        if(player.isBeingAttacked() || player.isLocked() || player.isDead() || player.interfaces.currentModal != -1) {
            timers[TIMER] = 10
            return@on_timer
        }
        player.interruptQueues()
        player.stopMovement()
        player.animate(-1)
        timers[LOGOUT_TIMER] = 200
        player.lockingQueue(TaskPriority.STRONG, lockState = LockState.FULL_WITH_DAMAGE_IMMUNITY) {
            val randomNumber = world.random(100)
            val amount = inputInt("Please answer with the following number: $randomNumber")
            if (amount == randomNumber) {
                if (!player.inventory.hasSpace) {
                    itemMessageBox("Thank you for solving this random event, a gift has been added to your bank.", item = Items.MYSTERY_BOX)
                    player.bank.add(Item(Items.MYSTERY_BOX))
                } else {
                    itemMessageBox("Thank you for solving this random event, a gift has been added to your inventory.", item = Items.MYSTERY_BOX)
                    player.inventory.add(Item(Items.MYSTERY_BOX))
                }
                player.addLoyalty(world.random(1..30))
                timers.remove(LOGOUT_TIMER)
                timers[TIMER] = world.random(range)
            } else {
                timers[TIMER] = 1
            }
        }
    }
}

on_timer(LOGOUT_TIMER) {
    // Use the `synchronized` block to ensure thread safety while calling `handleLogout` on the player
    synchronized(timers) {
        player.handleLogout()
    }
}