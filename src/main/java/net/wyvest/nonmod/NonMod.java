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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = "nonmod", name = "NonMod", version = "1.0")
public class NonMod {
    Pattern regex = Pattern.compile("^(?:[\\w\\- ]+ )?(?:(?<chatTypePrefix>[A-Za-z]+) > |)(?<stars>(?:\\[.*?]?)* )(?<tags>(?:\\[[^]]+] ?)*)(?<senderUsername>\\w{1,16})(?: [\\w\\- ]+)?: (?<message>.+)$");
    String[] ranks = new String[]{"youtube", "owner", "admin", "gm", "mvp", "vip", "mojang", "events", "mcp", "pig"};

    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void doThings(ClientChatReceivedEvent event) {
        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
        Matcher matcher = regex.matcher(unformattedText);
        if (matcher.matches()) {
            if (!containsAny(matcher.group("tags"), ranks)) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(addNonTag((ChatComponentText) event.message, matcher));
                event.setCanceled(true);
            }
        }
    }

    private boolean containsAny(String string, String[] list) {
        for (String check : list) {
            if (string.toLowerCase(Locale.ENGLISH).contains(check)) {
                return true;
            }
        }
        return false;
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
