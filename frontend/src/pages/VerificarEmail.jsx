import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { useApiFetch } from "../api/useApiFetch";

import Header from "../components/Header";
import Footer from "../components/Footer";
import "./VerificarEmail.css";

export default function VerificarEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [estado, setEstado] = useState("loading");
  const [mensaje, setMensaje] = useState("Verificando tu cuenta...");

  const authFetch = useApiFetch();

  useEffect(() => {
    const verificarCorreo = async () => {
      if (!token) {
        setEstado("error");
        setMensaje("Falta el token de verificación. Revisá el enlace del correo.");
        return;
      }

      try {
        const response = await authFetch("/auth/verificar?token=" + encodeURIComponent(token));

        const data = await response.json();

        setEstado("success");
        setMensaje(
          data.mensaje ||
          "¡Tu correo de verificación fue enviado correctamente!."
        );
      } catch (error) {
        console.error("Error al verificar el correo:", error);
        if (error.error === "TOKEN_YA_USADO") {
          setEstado("error");
          setMensaje(error.mensaje ||
            "Este correo ya fue verificado anteriormente. Podés iniciar sesión."
          );
          return;
        }

        setEstado("error");
        setMensaje(
          error.mensaje ||
          "No pudimos verificar tu correo."
        )
      }
    };

    verificarCorreo();
  }, [token, authFetch]);

  return (
    <>
      <Header />

      <main className="verificar-container">
        <div className="verificar-card">
          {estado === "loading" && <div className="verificar-spinner" />}

          {estado !== "loading" && (
            <div className={`verificar-icon ${estado}`}>
              {estado === "success" ? "✓" : "!"}
            </div>
          )}

          <h1 className="verificar-titulo">
            {estado === "success" && "Verificación exitosa"}
            {estado === "error" && "No pudimos verificar tu correo"}
            {estado === "loading" && "Verificando..."}
          </h1>

          <p className="verificar-mensaje">{mensaje}</p>

          {estado === "success" && (
            <a href="/" className="verificar-btn">Ir al catalogo</a>
          )}
          {estado === "error" && (
            <a href="/reenviar-verificacion" className="verificar-btn">Reenviar correo</a>
          )}
        </div>
      </main>

      <Footer />
    </>
  );
}