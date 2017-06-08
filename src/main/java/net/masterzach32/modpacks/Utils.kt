package net.masterzach32.modpacks

/*
 * MinecraftModpackManager - Created on 6/8/17
 * Author: Zachary Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zachary Kozar
 * @version 6/8/17
 */

fun String.split(): Array<String> {
    val temp = split(' ')
    val split = mutableListOf<String>()

    var i = 0
    while (i < temp.size) {
        if (temp[i].contains('"') && temp[i][0] == '"') {
            var str = temp[i].substring(1)
            while (i + 1 < temp.size && !temp[i+1].contains('"')) {
                str += " ${temp[i+1]}"
                i++
            }
            if (i + 1 < temp.size && temp[i+1].contains('"') && temp[i+1].last() == '"')
                str += " ${temp[i+1].substring(0, temp[i+1].lastIndex)}"
            split.add(str)
            i++
        } else
            split.add(temp[i])
        i++
    }

    return split.toTypedArray()
}