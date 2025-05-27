package winlyps.openDoors

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.type.Door
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.block.Action
import org.bukkit.plugin.java.JavaPlugin

class OpenDoors : JavaPlugin(), Listener {

    override fun onEnable() {
        // Register the event listener
        server.pluginManager.registerEvents(this, this)
        logger.info("OpenDoors plugin has been enabled!")
    }

    override fun onDisable() {
        logger.info("OpenDoors plugin has been disabled!")
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // Check if the player right-clicked on a block
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return

        // Check if the clicked block is a door (but not iron door)
        if (!isDoorButNotIron(clickedBlock)) return

        // Check if the door is currently open or closed to determine action
        val doorData = clickedBlock.blockData as? Door ?: return
        val isCurrentlyOpen = doorData.isOpen

        // Find and toggle adjacent doors
        if (isCurrentlyOpen) {
            // Door is open, so it will be closed - close adjacent doors too
            closeAdjacentDoors(clickedBlock)
        } else {
            // Door is closed, so it will be opened - open adjacent doors too
            openAdjacentDoors(clickedBlock)
        }
    }

    private fun isDoorButNotIron(block: Block): Boolean {
        val material = block.type
        return when (material) {
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,
            Material.MANGROVE_DOOR,
            Material.CHERRY_DOOR,
            Material.BAMBOO_DOOR,
            Material.CRIMSON_DOOR,
            Material.WARPED_DOOR -> true
            else -> false
        }
    }

    private fun openAdjacentDoors(centerBlock: Block) {
        val doorBlock = getDoorBottomBlock(centerBlock)
        val checkedBlocks = mutableSetOf<Block>()
        val doorsToOpen = mutableListOf<Block>()

        // Find all connected doors using BFS
        findConnectedDoors(doorBlock, checkedBlocks, doorsToOpen)

        // Open all found doors
        for (door in doorsToOpen) {
            openDoor(door)
        }
    }

    private fun closeAdjacentDoors(centerBlock: Block) {
        val doorBlock = getDoorBottomBlock(centerBlock)
        val checkedBlocks = mutableSetOf<Block>()
        val doorsToClose = mutableListOf<Block>()

        // Find all connected doors using BFS
        findConnectedDoors(doorBlock, checkedBlocks, doorsToClose)

        // Close all found doors
        for (door in doorsToClose) {
            closeDoor(door)
        }
    }

    private fun findConnectedDoors(startBlock: Block, checkedBlocks: MutableSet<Block>, doorsToProcess: MutableList<Block>) {
        val queue = mutableListOf(startBlock)

        while (queue.isNotEmpty()) {
            val currentBlock = queue.removeAt(0)

            if (checkedBlocks.contains(currentBlock)) continue
            checkedBlocks.add(currentBlock)

            if (isDoorButNotIron(currentBlock)) {
                val bottomBlock = getDoorBottomBlock(currentBlock)
                if (!doorsToProcess.contains(bottomBlock)) {
                    doorsToProcess.add(bottomBlock)
                }

                // Check adjacent blocks (6 directions: N, S, E, W, Up, Down)
                val adjacentBlocks = listOf(
                    currentBlock.getRelative(1, 0, 0),   // East
                    currentBlock.getRelative(-1, 0, 0),  // West
                    currentBlock.getRelative(0, 0, 1),   // South
                    currentBlock.getRelative(0, 0, -1),  // North
                    currentBlock.getRelative(0, 1, 0),   // Up
                    currentBlock.getRelative(0, -1, 0)   // Down
                )

                for (adjacent in adjacentBlocks) {
                    if (!checkedBlocks.contains(adjacent) && isDoorButNotIron(adjacent)) {
                        queue.add(adjacent)
                    }
                }
            }
        }
    }

    private fun getDoorBottomBlock(doorBlock: Block): Block {
        // Check if the block below is also a door of the same type
        val blockBelow = doorBlock.getRelative(0, -1, 0)
        return if (blockBelow.type == doorBlock.type && isDoorButNotIron(blockBelow)) {
            // Current block is the top half, return the bottom half
            blockBelow
        } else {
            // Current block is already the bottom half
            doorBlock
        }
    }

    private fun openDoor(doorBlock: Block) {
        val blockData = doorBlock.blockData
        if (blockData is Door) {
            // Only open the door if it's currently closed
            if (!blockData.isOpen) {
                blockData.isOpen = true
                doorBlock.blockData = blockData

                // Play door opening sound
                doorBlock.world.playSound(
                    doorBlock.location,
                    org.bukkit.Sound.BLOCK_WOODEN_DOOR_OPEN,
                    1.0f,
                    1.0f
                )
            }
        }
    }

    private fun closeDoor(doorBlock: Block) {
        val blockData = doorBlock.blockData
        if (blockData is Door) {
            // Only close the door if it's currently open
            if (blockData.isOpen) {
                blockData.isOpen = false
                doorBlock.blockData = blockData

                // Play door closing sound
                doorBlock.world.playSound(
                    doorBlock.location,
                    org.bukkit.Sound.BLOCK_WOODEN_DOOR_CLOSE,
                    1.0f,
                    1.0f
                )
            }
        }
    }
}