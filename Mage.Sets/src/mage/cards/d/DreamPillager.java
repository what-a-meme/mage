
package mage.cards.d;

import java.util.Set;
import java.util.UUID;
import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.AsThoughEffectImpl;
import mage.abilities.effects.ContinuousEffect;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.players.Player;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author LevelX2
 */
public final class DreamPillager extends CardImpl {

    public DreamPillager(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId,setInfo,new CardType[]{CardType.CREATURE},"{5}{R}{R}");
        this.subtype.add(SubType.DRAGON);
        this.power = new MageInt(4);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());
        // Whenever Dream Pillager deals combat damage to a player, exile that many cards from the top of your library. Until end of turn, you may cast nonland cards from among those exiled cards.
        this.addAbility(new DreamPillagerTriggeredAbility());
    }

    public DreamPillager(final DreamPillager card) {
        super(card);
    }

    @Override
    public DreamPillager copy() {
        return new DreamPillager(this);
    }
}

class DreamPillagerTriggeredAbility extends TriggeredAbilityImpl {

    public DreamPillagerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new DreamPillagerEffect(), false);
    }

    public DreamPillagerTriggeredAbility(final DreamPillagerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public DreamPillagerTriggeredAbility copy() {
        return new DreamPillagerTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (event.getSourceId().equals(this.sourceId) && ((DamagedPlayerEvent) event).isCombatDamage()) {
            for (Effect effect : getEffects()) {
                effect.setValue("damage", event.getAmount());
            }
            return true;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever {this} deals combat damage to a player, exile that many cards from the top of your library. Until end of turn, you may cast nonland cards from among those exiled cards.";
    }
}

class DreamPillagerEffect extends OneShotEffect {

    public DreamPillagerEffect() {
        super(Outcome.Benefit);
        this.staticText = "exile that many cards from the top of your library. Until end of turn, you may cast nonland cards from among those exiled cards";
    }

    public DreamPillagerEffect(final DreamPillagerEffect effect) {
        super(effect);
    }

    @Override
    public DreamPillagerEffect copy() {
        return new DreamPillagerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        MageObject sourceObject = source.getSourceObject(game);
        if (controller != null && sourceObject != null) {
            int amount = (Integer) getValue("damage");
            if (amount > 0) {
                Set<Card> cards = controller.getLibrary().getTopCards(game, amount);
                if (!cards.isEmpty()) {
                    controller.moveCards(cards, Zone.EXILED, source, game);
                    for (Card card : cards) {
                        if (!card.isLand()) {
                            ContinuousEffect effect = new DreamPillagerCastFromExileEffect();
                            effect.setTargetPointer(new FixedTarget(card.getId(), card.getZoneChangeCounter(game)));
                            game.addEffect(effect, source);
                        }
                    }
                }
                return true;
            }
            return true;
        }
        return false;
    }
}

class DreamPillagerCastFromExileEffect extends AsThoughEffectImpl {

    public DreamPillagerCastFromExileEffect() {
        super(AsThoughEffectType.PLAY_FROM_NOT_OWN_HAND_ZONE, Duration.EndOfTurn, Outcome.Benefit);
        staticText = "You may play the card from exile";
    }

    public DreamPillagerCastFromExileEffect(final DreamPillagerCastFromExileEffect effect) {
        super(effect);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        return true;
    }

    @Override
    public DreamPillagerCastFromExileEffect copy() {
        return new DreamPillagerCastFromExileEffect(this);
    }

    @Override
    public boolean applies(UUID objectId, Ability source, UUID affectedControllerId, Game game) {
        return source.isControlledBy(affectedControllerId)
                && objectId.equals(getTargetPointer().getFirst(game, source));
    }
}
