package net.wyvest.nonmod;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = "nonmod", name = "NonMod", version = "1.2")
public class NonMod {
    Pattern regex = Pattern.compile("^(?:[\\w\\- ]+ )?(?:(?<chatTypePrefix>[A-Za-z]+) > |)(?<tags>(?:\\[[^]]+] ?)*)(?<senderUsername>\\w{1,16})(?: [\\w\\- ]+)?: (?<message>.+)$");

    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void doThings(ClientChatReceivedEvent event) {
        if (!(event.message instanceof ChatComponentText) return;
        ChatComponentText text = (ChatComponentText) event.message;
        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(text.getUnformattedText());
        Matcher matcher = regex.matcher(unformattedText);
        if (matcher.matches()) {
            String substringBefore = StringUtils.substringBefore(text.getFormattedText(), matcher.group("senderUsername").substring(1));
            if (substringBefore.charAt(substringBefore.lastIndexOf("§") + 1) == '7') {
                Minecraft.getMinecraft().thePlayer.addChatMessage(addNonTag(text, matcher));
                event.setCanceled(true);
            } else if (substringBefore.charAt(substringBefore.lastIndexOf("§") + 1) == 'r') {
                if (substringBefore.charAt(substringBefore.lastIndexOf("§", substringBefore.lastIndexOf("§"))) == '7') {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(addNonTag(text, matcher));
                    event.setCanceled(true);
                }
            }
        }
    }

    private ChatComponentText addNonTag(ChatComponentText message, Matcher matcher) {
        String oldText = message.getUnformattedTextForChat();
        ChatComponentText newText = (ChatComponentText) new ChatComponentText(oldText).setChatStyle(message.getChatStyle());
        boolean found = false;
        for (IChatComponent sibling : message.getSiblings()) {
            if (sibling.getUnformattedText().contains(matcher.group("senderUsername")) && !found) {
                found = true;
                newText.appendSibling(new ChatComponentText(sibling.getUnformattedText().replaceFirst(Pattern.compile(matcher.group("senderUsername"), Pattern.LITERAL).toString(), "[NON] " + matcher.group("senderUsername"))).setChatStyle(sibling.getChatStyle()));
            } else {
                newText.appendSibling(new ChatComponentText(sibling.getUnformattedText()).setChatStyle(sibling.getChatStyle()));
            }
        }
        return newText;
    }
}
