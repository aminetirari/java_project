"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api, extractErrorMessage } from "@/lib/api";
import type { Address, Cart } from "@/lib/types";
import { useAuthStore } from "@/store/authStore";
import { useCartStore } from "@/store/cartStore";
import Loader from "@/components/Loader";
import ErrorBox from "@/components/ErrorBox";
import EmptyState from "@/components/EmptyState";

export default function CheckoutPage() {
  const router = useRouter();
  const token = useAuthStore((s) => s.token);
  const { cart, setCart } = useCartStore();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddress, setSelectedAddress] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [rue, setRue] = useState("");
  const [ville, setVille] = useState("");
  const [codePostal, setCodePostal] = useState("");
  const [pays, setPays] = useState("France");

  useEffect(() => {
    if (!token) {
      router.replace("/login?redirect=/checkout");
      return;
    }
    Promise.all([
      api.get<Cart>("/cart"),
      api.get<Address[]>("/addresses"),
    ])
      .then(([c, a]) => {
        setCart(c.data);
        setAddresses(a.data);
        const principal = a.data.find((x) => x.principal) ?? a.data[0];
        if (principal) setSelectedAddress(principal.id);
      })
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [token, router, setCart]);

  const addAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.post<Address>("/addresses", {
        rue,
        ville,
        codePostal,
        pays,
        principal: addresses.length === 0,
      });
      setAddresses((prev) => [...prev, data]);
      setSelectedAddress(data.id);
      setRue("");
      setVille("");
      setCodePostal("");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const placeOrder = async () => {
    if (!selectedAddress) {
      setError("Veuillez choisir ou ajouter une adresse de livraison");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.post("/orders", { addressId: selectedAddress });
      setCart({ ...cart!, lignes: [], sousTotal: 0, remise: 0, totalCart: 0 });
      router.push(`/profile/orders/${data.id}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  if (loading) return <Loader />;
  if (!cart || cart.lignes.length === 0)
    return (
      <EmptyState
        title="Votre panier est vide"
        description="Ajoutez des produits avant de passer commande."
        ctaHref="/products"
        ctaLabel="Voir le catalogue"
      />
    );

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <section className="space-y-6">
        <h1 className="section-title">Finaliser la commande</h1>
        <ErrorBox message={error ?? undefined} />

        <div className="card space-y-3 p-5">
          <h2 className="text-lg font-semibold">Adresse de livraison</h2>
          {addresses.length === 0 ? (
            <p className="text-sm text-slate-500">
              Vous n&apos;avez pas encore d&apos;adresse enregistrée.
            </p>
          ) : (
            <div className="space-y-2">
              {addresses.map((a) => (
                <label
                  key={a.id}
                  className={`flex cursor-pointer items-start gap-3 rounded-lg border px-3 py-2 text-sm ${
                    selectedAddress === a.id
                      ? "border-indigo-600 bg-indigo-50"
                      : "border-slate-200 hover:border-slate-300"
                  }`}
                >
                  <input
                    type="radio"
                    name="address"
                    className="mt-1"
                    checked={selectedAddress === a.id}
                    onChange={() => setSelectedAddress(a.id)}
                  />
                  <div>
                    <div className="font-medium">{a.rue}</div>
                    <div className="text-slate-500">
                      {a.codePostal} {a.ville}, {a.pays}
                    </div>
                  </div>
                </label>
              ))}
            </div>
          )}

          <form onSubmit={addAddress} className="grid gap-2 border-t border-slate-200 pt-4 sm:grid-cols-2">
            <h3 className="col-span-full text-sm font-semibold text-slate-700">
              Ajouter une adresse
            </h3>
            <input
              className="input"
              placeholder="Rue"
              value={rue}
              onChange={(e) => setRue(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="Ville"
              value={ville}
              onChange={(e) => setVille(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="Code postal"
              value={codePostal}
              onChange={(e) => setCodePostal(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="Pays"
              value={pays}
              onChange={(e) => setPays(e.target.value)}
              required
            />
            <button type="submit" disabled={busy} className="btn-outline col-span-full">
              Enregistrer l&apos;adresse
            </button>
          </form>
        </div>
      </section>

      <aside className="card sticky top-20 h-fit space-y-3 p-5">
        <h2 className="text-lg font-semibold">Votre commande</h2>
        <ul className="divide-y divide-slate-100 text-sm">
          {cart.lignes.map((l) => (
            <li key={l.id} className="flex justify-between py-2">
              <span className="truncate pr-2">
                {l.productNom} × {l.quantite}
              </span>
              <span className="whitespace-nowrap">{l.sousTotal.toFixed(2)} €</span>
            </li>
          ))}
        </ul>
        <div className="space-y-1 border-t border-slate-200 pt-2 text-sm">
          <div className="flex justify-between">
            <span>Sous-total</span>
            <span>{cart.sousTotal.toFixed(2)} €</span>
          </div>
          {cart.remise > 0 && (
            <div className="flex justify-between text-emerald-600">
              <span>Remise</span>
              <span>-{cart.remise.toFixed(2)} €</span>
            </div>
          )}
          <div className="flex justify-between text-base font-bold">
            <span>Total</span>
            <span>{cart.totalCart.toFixed(2)} €</span>
          </div>
        </div>
        <button
          type="button"
          onClick={placeOrder}
          disabled={busy || !selectedAddress}
          className="btn-primary w-full"
        >
          {busy ? "Traitement..." : "Confirmer la commande"}
        </button>
        <Link href="/cart" className="block text-center text-sm text-slate-500 hover:text-indigo-600">
          ← Modifier le panier
        </Link>
      </aside>
    </div>
  );
}
